package lk.oceanview.servlet;

import lk.oceanview.config.DBConnection;
import lk.oceanview.model.User;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ReportServlet
 * GET -> show reports dashboard with revenue, occupancy and booking stats
 */
public class ReportServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Session check
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedInUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login.html?error=session");
            return;
        }

        User   user        = (User) session.getAttribute("loggedInUser");
        String contextPath = request.getContextPath();

        // Date range filter - default to current month
        String fromDate = request.getParameter("fromDate");
        String toDate   = request.getParameter("toDate");

        if (fromDate == null || fromDate.isEmpty()) {
            fromDate = LocalDate.now().withDayOfMonth(1).toString();
        }
        if (toDate == null || toDate.isEmpty()) {
            toDate = LocalDate.now().toString();
        }

        // ---- Fetch all report data ----
        double totalRevenue       = getTotalRevenue(fromDate, toDate);
        int    totalReservations  = getTotalReservations(fromDate, toDate);
        int    checkedOutCount    = getCountByStatus("CHECKED_OUT", fromDate, toDate);
        int    cancelledCount     = getCountByStatus("CANCELLED",   fromDate, toDate);
        int    confirmedCount     = getCountByStatus("CONFIRMED",   fromDate, toDate);
        int    checkedInCount     = getCountByStatus("CHECKED_IN",  fromDate, toDate);
        double avgStayDuration    = getAvgStayDuration(fromDate, toDate);
        double occupancyRate      = getOccupancyRate();

        Map<String, Double>  revenueByRoomType = getRevenueByRoomType(fromDate, toDate);
        Map<String, Integer> bookingsByMonth   = getBookingsByMonth();
        List<String[]>       topGuests         = getTopGuests(fromDate, toDate);
        List<String[]>       recentBills        = getRecentBills(fromDate, toDate);

        // ---- Render page ----
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        printHeader(out, contextPath, user, fromDate, toDate);

        out.println("<div class='main'>");
        out.println("<div class='page-header'>");
        out.println("<div style='display:flex;justify-content:space-between;align-items:flex-start;'>");
        out.println("<div><h1>📊 Reports</h1><p>Revenue and occupancy statistics for Ocean View Resort</p></div>");
        out.println("<button onclick='window.print()' class='btn btn-primary'>🖨 Print Report</button>");
        out.println("</div></div>");

        // ---- Date filter ----
        out.println("<div class='card filter-card no-print'>");
        out.println("<form method='GET' action='" + contextPath + "/report' style='display:flex;gap:16px;align-items:flex-end;flex-wrap:wrap;'>");
        out.println("<div class='form-group'><label>From Date</label><input type='date' name='fromDate' value='" + fromDate + "' /></div>");
        out.println("<div class='form-group'><label>To Date</label><input type='date' name='toDate' value='" + toDate + "' /></div>");
        out.println("<button type='submit' class='btn btn-primary'>🔍 Generate Report</button>");
        out.println("<a href='" + contextPath + "/report' class='btn btn-secondary'>Reset</a>");
        out.println("</form></div>");

        // ---- Date range label ----
        out.println("<div class='date-range-label'>📅 Showing data from <strong>" + fromDate + "</strong> to <strong>" + toDate + "</strong></div>");

        // ---- Key Stats ----
        out.println("<div class='stats-grid'>");
        printStatCard(out, "💰", "Total Revenue",      "LKR " + String.format("%,.2f", totalRevenue),  "blue");
        printStatCard(out, "📋", "Total Reservations", String.valueOf(totalReservations),               "purple");
        printStatCard(out, "📈", "Occupancy Rate",     String.format("%.1f", occupancyRate) + "%",      "green");
        printStatCard(out, "🌙", "Avg Stay Duration",  String.format("%.1f", avgStayDuration) + " nights", "orange");
        out.println("</div>");

        // ---- Reservation Status Breakdown ----
        out.println("<div class='two-col'>");

        out.println("<div class='card'>");
        out.println("<h2>📋 Reservation Status Breakdown</h2>");
        out.println("<div class='status-bars'>");
        printStatusBar(out, "✅ Confirmed",    confirmedCount,  totalReservations, "#3b82f6");
        printStatusBar(out, "🏨 Checked In",   checkedInCount,  totalReservations, "#16a085");
        printStatusBar(out, "🚪 Checked Out",  checkedOutCount, totalReservations, "#6b7280");
        printStatusBar(out, "❌ Cancelled",    cancelledCount,  totalReservations, "#dc2626");
        out.println("</div></div>");

        // ---- Revenue by Room Type ----
        out.println("<div class='card'>");
        out.println("<h2>🛏 Revenue by Room Type</h2>");
        if (revenueByRoomType.isEmpty()) {
            out.println("<p style='color:#aaa;text-align:center;padding:30px;'>No revenue data available.</p>");
        } else {
            double maxRevenue = revenueByRoomType.values().stream().mapToDouble(Double::doubleValue).max().orElse(1);
            out.println("<div class='status-bars'>");
            for (Map.Entry<String, Double> entry : revenueByRoomType.entrySet()) {
                int pct = (int) ((entry.getValue() / maxRevenue) * 100);
                out.println("<div class='bar-row'>");
                out.println("<div class='bar-label'>" + entry.getKey() + "</div>");
                out.println("<div class='bar-track'><div class='bar-fill' style='width:" + pct + "%;background:linear-gradient(135deg,#0f4c75,#1b6ca8);'></div></div>");
                out.println("<div class='bar-value'>LKR " + String.format("%,.0f", entry.getValue()) + "</div>");
                out.println("</div>");
            }
            out.println("</div>");
        }
        out.println("</div>");
        out.println("</div>"); // end two-col

        // ---- Monthly Bookings Trend ----
        out.println("<div class='card'>");
        out.println("<h2>📅 Monthly Bookings Trend (Last 6 Months)</h2>");
        if (bookingsByMonth.isEmpty()) {
            out.println("<p style='color:#aaa;text-align:center;padding:30px;'>No booking data available.</p>");
        } else {
            int maxBookings = bookingsByMonth.values().stream().mapToInt(Integer::intValue).max().orElse(1);
            out.println("<div class='bar-chart'>");
            for (Map.Entry<String, Integer> entry : bookingsByMonth.entrySet()) {
                int pct = maxBookings > 0 ? (int) ((entry.getValue() / (double) maxBookings) * 180) : 0;
                out.println("<div class='bar-col'>");
                out.println("<div class='bar-count'>" + entry.getValue() + "</div>");
                out.println("<div class='bar-block' style='height:" + pct + "px;'></div>");
                out.println("<div class='bar-month'>" + entry.getKey() + "</div>");
                out.println("</div>");
            }
            out.println("</div>");
        }
        out.println("</div>");

        // ---- Two column: Top Guests + Recent Bills ----
        out.println("<div class='two-col'>");

        // Top Guests
        out.println("<div class='card'>");
        out.println("<h2>🏆 Top Guests by Visits</h2>");
        if (topGuests.isEmpty()) {
            out.println("<p style='color:#aaa;text-align:center;padding:30px;'>No guest data available.</p>");
        } else {
            out.println("<table><thead><tr><th>#</th><th>Guest Name</th><th>Visits</th><th>Total Spent (LKR)</th></tr></thead><tbody>");
            int rank = 1;
            for (String[] g : topGuests) {
                String medal = rank == 1 ? "🥇" : rank == 2 ? "🥈" : rank == 3 ? "🥉" : String.valueOf(rank);
                out.println("<tr><td>" + medal + "</td><td><strong>" + g[0] + "</strong></td><td>" + g[1] + "</td><td>" + String.format("%,.2f", Double.parseDouble(g[2])) + "</td></tr>");
                rank++;
            }
            out.println("</tbody></table>");
        }
        out.println("</div>");

        // Recent Bills
        out.println("<div class='card'>");
        out.println("<h2>🧾 Recent Bills</h2>");
        if (recentBills.isEmpty()) {
            out.println("<p style='color:#aaa;text-align:center;padding:30px;'>No billing data available.</p>");
        } else {
            out.println("<table><thead><tr><th>Reservation</th><th>Guest</th><th>Total (LKR)</th><th>Status</th></tr></thead><tbody>");
            for (String[] b : recentBills) {
                String badgeClass = "PAID".equals(b[3]) ? "badge-paid" : "badge-pending";
                out.println("<tr><td><strong>" + b[0] + "</strong></td><td>" + b[1] + "</td><td>" + String.format("%,.2f", Double.parseDouble(b[2])) + "</td>");
                out.println("<td><span class='badge " + badgeClass + "'>" + b[3] + "</span></td></tr>");
            }
            out.println("</tbody></table>");
        }
        out.println("</div>");
        out.println("</div>"); // end two-col

        out.println("</div>"); // end main

        printFooter(out);
    }

    // ================================================
    // DATA QUERIES
    // ================================================

    private double getTotalRevenue(String from, String to) {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM bills b " +
                     "JOIN reservations r ON b.reservation_id = r.reservation_id " +
                     "WHERE r.check_in_date BETWEEN ? AND ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, from); ps.setString(2, to);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("[ReportServlet] getTotalRevenue: " + e.getMessage());
        }
        return 0;
    }

    private int getTotalReservations(String from, String to) {
        String sql = "SELECT COUNT(*) FROM reservations WHERE check_in_date BETWEEN ? AND ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, from); ps.setString(2, to);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[ReportServlet] getTotalReservations: " + e.getMessage());
        }
        return 0;
    }

    private int getCountByStatus(String status, String from, String to) {
        String sql = "SELECT COUNT(*) FROM reservations WHERE status = ? AND check_in_date BETWEEN ? AND ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status); ps.setString(2, from); ps.setString(3, to);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[ReportServlet] getCountByStatus: " + e.getMessage());
        }
        return 0;
    }

    private double getAvgStayDuration(String from, String to) {
        String sql = "SELECT COALESCE(AVG(DATEDIFF(check_out_date, check_in_date)), 0) " +
                     "FROM reservations WHERE check_in_date BETWEEN ? AND ? AND status != 'CANCELLED'";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, from); ps.setString(2, to);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("[ReportServlet] getAvgStayDuration: " + e.getMessage());
        }
        return 0;
    }

    private double getOccupancyRate() {
        String sql = "SELECT " +
                     "(SELECT COUNT(*) FROM rooms WHERE status = 'OCCUPIED') * 100.0 / " +
                     "(SELECT COUNT(*) FROM rooms)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("[ReportServlet] getOccupancyRate: " + e.getMessage());
        }
        return 0;
    }

    private Map<String, Double> getRevenueByRoomType(String from, String to) {
        Map<String, Double> map = new LinkedHashMap<>();
        String sql = "SELECT rt.type_name, COALESCE(SUM(b.total_amount), 0) as revenue " +
                     "FROM room_types rt " +
                     "JOIN rooms ro ON rt.room_type_id = ro.room_type_id " +
                     "JOIN reservations r ON ro.room_id = r.room_id " +
                     "JOIN bills b ON r.reservation_id = b.reservation_id " +
                     "WHERE r.check_in_date BETWEEN ? AND ? " +
                     "GROUP BY rt.type_name ORDER BY revenue DESC";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, from); ps.setString(2, to);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) map.put(rs.getString("type_name"), rs.getDouble("revenue"));
        } catch (SQLException e) {
            System.err.println("[ReportServlet] getRevenueByRoomType: " + e.getMessage());
        }
        return map;
    }

    private Map<String, Integer> getBookingsByMonth() {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = "SELECT DATE_FORMAT(check_in_date, '%b %Y') as month, COUNT(*) as total " +
                     "FROM reservations " +
                     "WHERE check_in_date >= DATE_SUB(CURDATE(), INTERVAL 6 MONTH) " +
                     "GROUP BY DATE_FORMAT(check_in_date, '%Y-%m') " +
                     "ORDER BY MIN(check_in_date)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) map.put(rs.getString("month"), rs.getInt("total"));
        } catch (SQLException e) {
            System.err.println("[ReportServlet] getBookingsByMonth: " + e.getMessage());
        }
        return map;
    }

    private List<String[]> getTopGuests(String from, String to) {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT g.full_name, COUNT(r.reservation_id) as visits, " +
                     "COALESCE(SUM(b.total_amount), 0) as total_spent " +
                     "FROM guests g " +
                     "JOIN reservations r ON g.guest_id = r.guest_id " +
                     "LEFT JOIN bills b ON r.reservation_id = b.reservation_id " +
                     "WHERE r.check_in_date BETWEEN ? AND ? " +
                     "GROUP BY g.guest_id, g.full_name " +
                     "ORDER BY visits DESC, total_spent DESC LIMIT 5";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, from); ps.setString(2, to);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                list.add(new String[]{rs.getString("full_name"), String.valueOf(rs.getInt("visits")), String.valueOf(rs.getDouble("total_spent"))});
        } catch (SQLException e) {
            System.err.println("[ReportServlet] getTopGuests: " + e.getMessage());
        }
        return list;
    }

    private List<String[]> getRecentBills(String from, String to) {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT r.reservation_number, g.full_name, b.total_amount, b.payment_status " +
                     "FROM bills b " +
                     "JOIN reservations r ON b.reservation_id = r.reservation_id " +
                     "JOIN guests g ON r.guest_id = g.guest_id " +
                     "WHERE r.check_in_date BETWEEN ? AND ? " +
                     "ORDER BY b.bill_id DESC LIMIT 8";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, from); ps.setString(2, to);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                list.add(new String[]{rs.getString("reservation_number"), rs.getString("full_name"), String.valueOf(rs.getDouble("total_amount")), rs.getString("payment_status")});
        } catch (SQLException e) {
            System.err.println("[ReportServlet] getRecentBills: " + e.getMessage());
        }
        return list;
    }

    // ================================================
    // UI HELPERS
    // ================================================

    private void printStatCard(PrintWriter out, String icon, String label, String value, String color) {
        out.println("<div class='stat-card " + color + "'>");
        out.println("<div><div class='number'>" + value + "</div><div class='label'>" + label + "</div></div>");
        out.println("<div class='icon'>" + icon + "</div></div>");
    }

    private void printStatusBar(PrintWriter out, String label, int count, int total, String color) {
        int pct = total > 0 ? (int) ((count / (double) total) * 100) : 0;
        out.println("<div class='bar-row'>");
        out.println("<div class='bar-label'>" + label + "</div>");
        out.println("<div class='bar-track'><div class='bar-fill' style='width:" + pct + "%;background:" + color + ";'></div></div>");
        out.println("<div class='bar-value'>" + count + " (" + pct + "%)</div>");
        out.println("</div>");
    }

    // ================================================
    // HEADER & FOOTER
    // ================================================
    private void printHeader(PrintWriter out, String contextPath, User user,
                             String fromDate, String toDate) {
        out.println("<!DOCTYPE html><html lang='en'><head>");
        out.println("<meta charset='UTF-8'><meta name='viewport' content='width=device-width,initial-scale=1.0'>");
        out.println("<title>Reports | Ocean View Resort</title>");
        out.println("<style>");
        out.println("* { margin:0; padding:0; box-sizing:border-box; }");
        out.println("body { font-family:'Segoe UI',sans-serif; background:#f0f4f8; }");

        // Sidebar
        out.println(".sidebar { width:240px; height:100vh; background:linear-gradient(180deg,#0f4c75,#1b6ca8); position:fixed; top:0; left:0; padding:30px 0; overflow-y:auto; }");
        out.println(".sidebar .logo { text-align:center; padding:0 20px 30px; border-bottom:1px solid rgba(255,255,255,0.15); }");
        out.println(".sidebar .logo h2 { font-size:16px; font-weight:700; color:white; }");
        out.println(".sidebar nav a { display:block; padding:12px 25px; color:rgba(255,255,255,0.85); text-decoration:none; font-size:14px; }");
        out.println(".sidebar nav a:hover,.sidebar nav a.active { background:rgba(255,255,255,0.15); border-left:3px solid white; color:white; }");
        out.println(".sidebar .logout { position:absolute; bottom:20px; width:100%; padding:0 20px; }");
        out.println(".sidebar .logout a { display:block; padding:11px; text-align:center; background:rgba(255,255,255,0.15); color:white; border-radius:8px; text-decoration:none; font-size:13px; }");
        out.println(".sidebar .logout a:hover { background:rgba(255,80,80,0.4); }");

        // Main
        out.println(".main { margin-left:240px; padding:30px; }");
        out.println(".page-header { margin-bottom:24px; }");
        out.println(".page-header h1 { font-size:22px; color:#1a1a2e; font-weight:700; }");
        out.println(".page-header p  { color:#888; font-size:13px; margin-top:4px; }");

        // Stats
        out.println(".stats-grid { display:grid; grid-template-columns:repeat(4,1fr); gap:20px; margin-bottom:24px; }");
        out.println(".stat-card { background:white; border-radius:14px; padding:22px 24px; box-shadow:0 2px 12px rgba(0,0,0,0.07); border-left:4px solid; display:flex; justify-content:space-between; align-items:center; }");
        out.println(".stat-card.blue{border-color:#1b6ca8;} .stat-card.green{border-color:#16a085;} .stat-card.purple{border-color:#8e44ad;} .stat-card.orange{border-color:#e67e22;}");
        out.println(".stat-card .number { font-size:20px; font-weight:800; color:#1a1a2e; }");
        out.println(".stat-card .label  { font-size:12px; color:#888; margin-top:4px; }");
        out.println(".stat-card .icon   { font-size:28px; }");

        // Card
        out.println(".card { background:white; border-radius:14px; padding:24px; box-shadow:0 2px 12px rgba(0,0,0,0.07); margin-bottom:24px; }");
        out.println(".card h2 { font-size:16px; font-weight:700; color:#1a1a2e; margin-bottom:20px; padding-bottom:12px; border-bottom:2px solid #f0f4f8; }");
        out.println(".filter-card { padding:20px 24px; }");
        out.println(".date-range-label { font-size:13px; color:#666; margin-bottom:20px; background:white; padding:10px 18px; border-radius:10px; border-left:4px solid #1b6ca8; box-shadow:0 2px 8px rgba(0,0,0,0.05); }");

        // Two col
        out.println(".two-col { display:grid; grid-template-columns:1fr 1fr; gap:24px; }");

        // Bar charts (horizontal)
        out.println(".status-bars { display:flex; flex-direction:column; gap:14px; }");
        out.println(".bar-row { display:grid; grid-template-columns:140px 1fr 100px; gap:12px; align-items:center; }");
        out.println(".bar-label { font-size:13px; color:#444; font-weight:500; }");
        out.println(".bar-track { background:#f0f4f8; border-radius:20px; height:10px; overflow:hidden; }");
        out.println(".bar-fill  { height:100%; border-radius:20px; transition:width 0.5s ease; }");
        out.println(".bar-value { font-size:12px; color:#666; font-weight:600; text-align:right; }");

        // Bar chart (vertical / monthly)
        out.println(".bar-chart { display:flex; gap:16px; align-items:flex-end; justify-content:center; padding:20px 0 0; height:240px; }");
        out.println(".bar-col { display:flex; flex-direction:column; align-items:center; gap:6px; flex:1; }");
        out.println(".bar-count { font-size:12px; font-weight:700; color:#1b6ca8; }");
        out.println(".bar-block { width:100%; background:linear-gradient(180deg,#1b6ca8,#0f4c75); border-radius:6px 6px 0 0; min-height:4px; }");
        out.println(".bar-month { font-size:11px; color:#888; font-weight:600; white-space:nowrap; }");

        // Table
        out.println("table { width:100%; border-collapse:collapse; }");
        out.println("th { background:#f8fafc; padding:10px 14px; text-align:left; font-size:12px; font-weight:700; color:#666; text-transform:uppercase; border-bottom:2px solid #f0f4f8; }");
        out.println("td { padding:12px 14px; font-size:13px; color:#333; border-bottom:1px solid #f8fafc; }");
        out.println("tr:hover td { background:#fafbff; }");

        // Badges
        out.println(".badge { padding:4px 10px; border-radius:20px; font-size:11px; font-weight:700; }");
        out.println(".badge-paid    { background:#d1fae5; color:#065f46; }");
        out.println(".badge-pending { background:#fef3c7; color:#92400e; }");

        // Buttons
        out.println(".btn { padding:10px 20px; border:none; border-radius:10px; font-size:13px; font-weight:600; cursor:pointer; text-decoration:none; display:inline-block; transition:transform 0.2s; }");
        out.println(".btn-primary   { background:linear-gradient(135deg,#0f4c75,#1b6ca8); color:white; }");
        out.println(".btn-secondary { background:#f0f4f8; color:#555; }");
        out.println(".btn:hover { transform:translateY(-2px); }");

        // Form
        out.println(".form-group { display:flex; flex-direction:column; gap:6px; }");
        out.println(".form-group label { font-size:13px; font-weight:600; color:#444; }");
        out.println(".form-group input { padding:10px 14px; border:1.5px solid #e0e0e0; border-radius:10px; font-size:14px; outline:none; }");
        out.println(".form-group input:focus { border-color:#1b6ca8; }");

        // Print styles
        out.println("@media print {");
        out.println("  .sidebar, .no-print { display:none !important; }");
        out.println("  .main { margin-left:0 !important; padding:20px !important; }");
        out.println("  .card { box-shadow:none; border:1px solid #e0e0e0; }");
        out.println("  .two-col { grid-template-columns:1fr 1fr; }");
        out.println("}");

        out.println("</style></head><body>");

        // Sidebar
        out.println("<div class='sidebar'><div class='logo'><h2>🏨 Ocean View</h2></div><nav>");
        out.println("<a href='" + contextPath + "/dashboard'>🏠 Dashboard</a>");
        out.println("<a href='" + contextPath + "/reservation?action=add'>➕ New Reservation</a>");
        out.println("<a href='" + contextPath + "/reservation?action=list'>📋 All Reservations</a>");
        out.println("<a href='" + contextPath + "/room?action=list'>🛏 Rooms</a>");
        out.println("<a href='" + contextPath + "/service?action=list'>⭐ Services</a>");
        out.println("<a href='" + contextPath + "/bill'>💰 Billing</a>");
        out.println("<a href='" + contextPath + "/report' class='active'>📊 Reports</a>");
        out.println("<a href='" + contextPath + "/profile'>👤 My Profile</a>");
        if (user.isAdmin()) out.println("<a href='" + contextPath + "/user?action=list'>👥 Manage Users</a>");
        out.println("<a href='" + contextPath + "/help'>❓ Help & Guide</a>");
        out.println("</nav><div class='logout'><a href='" + contextPath + "/logout'>🚪 Logout</a></div></div>");
    }

    private void printFooter(PrintWriter out) {
        out.println("</body></html>");
    }
}