package lk.oceanview.servlet;

import com.google.gson.Gson;
import lk.oceanview.config.DBConnection;
import lk.oceanview.dao.GuestDAO;
import lk.oceanview.model.Guest;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GuestServlet
 * GET  ?action=lookup&nic=XXXXX   -> JSON: auto-fill guest details (reservation form)
 * GET  ?action=history            -> HTML: guest search + reservation history page
 * GET  ?action=history&guestId=X  -> HTML: specific guest's full history
 * POST action=add                 -> JSON: register a new guest
 */
public class GuestServlet extends HttpServlet {

    private GuestDAO guestDAO;
    private Gson     gson;

    @Override
    public void init() throws ServletException {
        guestDAO = new GuestDAO();
        gson     = new Gson();
    }

    // ------------------------------------------------
    // GET
    // ------------------------------------------------
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedInUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login.html?error=session");
            return;
        }

        String action = request.getParameter("action");

        if ("history".equals(action)) {
            showGuestHistory(request, response, session);
        } else {
            // Default: JSON lookup for reservation form auto-fill
            handleJsonLookup(request, response);
        }
    }

    // ------------------------------------------------
    // POST - add new guest (JSON response)
    // ------------------------------------------------
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedInUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login.html?error=session");
            return;
        }

        String fullName      = request.getParameter("fullName");
        String address       = request.getParameter("address");
        String contactNumber = request.getParameter("contactNumber");
        String email         = request.getParameter("email");
        String nicNumber     = request.getParameter("nicNumber");

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        if (fullName == null || fullName.trim().isEmpty() ||
            address == null || address.trim().isEmpty() ||
            contactNumber == null || contactNumber.trim().isEmpty()) {
            out.print("{\"success\": false, \"message\": \"Required fields are missing.\"}");
            return;
        }

        if (nicNumber != null && !nicNumber.trim().isEmpty()) {
            Guest existing = guestDAO.getGuestByNIC(nicNumber.trim());
            if (existing != null) {
                out.print("{\"success\": true, \"guestId\": " + existing.getGuestId() + ", \"existing\": true}");
                return;
            }
        }

        Guest guest = new Guest();
        guest.setFullName(fullName.trim());
        guest.setAddress(address.trim());
        guest.setContactNumber(contactNumber.trim());
        guest.setEmail(email != null ? email.trim() : "");
        guest.setNicNumber(nicNumber != null ? nicNumber.trim() : "");

        int guestId = guestDAO.addGuest(guest);
        if (guestId > 0) {
            out.print("{\"success\": true, \"guestId\": " + guestId + ", \"existing\": false}");
        } else {
            out.print("{\"success\": false, \"message\": \"Failed to save guest. Please try again.\"}");
        }
    }

    // ================================================
    // JSON Lookup (for reservation form auto-fill)
    // ================================================
    private void handleJsonLookup(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        String nic = request.getParameter("nic");
        if (nic != null && !nic.trim().isEmpty()) {
            Guest guest = guestDAO.getGuestByNIC(nic.trim());
            if (guest != null) {
                Map<String, Object> result = new HashMap<>();
                result.put("found",         true);
                result.put("guestId",       guest.getGuestId());
                result.put("fullName",      guest.getFullName());
                result.put("address",       guest.getAddress());
                result.put("contactNumber", guest.getContactNumber());
                result.put("email",         guest.getEmail());
                out.print(gson.toJson(result));
                return;
            }
        }
        out.print("{\"found\": false}");
    }

    // ================================================
    // Guest History Page
    // ================================================
    private void showGuestHistory(HttpServletRequest request, HttpServletResponse response,
                                  HttpSession session) throws IOException {

        User   user        = (User) session.getAttribute("loggedInUser");
        String contextPath = request.getContextPath();
        String searchQuery = request.getParameter("search");
        String guestIdStr  = request.getParameter("guestId");

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        printHeader(out, contextPath, user);
        out.println("<div class='main'>");
        out.println("<div class='page-header'><h1>👤 Guest History</h1><p>Search for a guest and view their complete reservation history</p></div>");

        // ---- Search Bar ----
        out.println("<div class='card search-card'>");
        out.println("<form method='GET' action='" + contextPath + "/guest' style='display:flex;gap:14px;align-items:flex-end;flex-wrap:wrap;'>");
        out.println("<input type='hidden' name='action' value='history'>");
        out.println("<div class='form-group' style='flex:1;min-width:220px;'>");
        out.println("<label>Search Guest by Name or NIC</label>");
        out.println("<input type='text' name='search' value='" + (searchQuery != null ? searchQuery : "") + "' placeholder='e.g. Kasun Perera or 123456789V' />");
        out.println("</div>");
        out.println("<button type='submit' class='btn btn-primary'>🔍 Search</button>");
        if (searchQuery != null) out.println("<a href='" + contextPath + "/guest?action=history' class='btn btn-secondary'>Clear</a>");
        out.println("</form></div>");

        // ---- Search Results ----
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            List<Guest> results = searchGuests(searchQuery.trim());
            if (results.isEmpty()) {
                out.println("<div class='alert alert-error'>⚠ No guests found matching \"" + searchQuery + "\".</div>");
            } else if (guestIdStr == null) {
                // Show list of matching guests
                out.println("<div class='card'>");
                out.println("<h2>🔍 Search Results for \"" + searchQuery + "\" (" + results.size() + " found)</h2>");
                out.println("<table><thead><tr><th>Full Name</th><th>NIC / Passport</th><th>Contact</th><th>Email</th><th>Action</th></tr></thead><tbody>");
                for (Guest g : results) {
                    out.println("<tr>");
                    out.println("<td><strong>" + g.getFullName() + "</strong></td>");
                    out.println("<td>" + (g.getNicNumber() != null && !g.getNicNumber().isEmpty() ? g.getNicNumber() : "-") + "</td>");
                    out.println("<td>" + (g.getContactNumber() != null ? g.getContactNumber() : "-") + "</td>");
                    out.println("<td>" + (g.getEmail() != null && !g.getEmail().isEmpty() ? g.getEmail() : "-") + "</td>");
                    out.println("<td><a href='" + contextPath + "/guest?action=history&search=" + searchQuery + "&guestId=" + g.getGuestId() + "' class='view-link'>View History →</a></td>");
                    out.println("</tr>");
                }
                out.println("</tbody></table></div>");
            }
        }

        // ---- Guest Reservation History ----
        if (guestIdStr != null && !guestIdStr.isEmpty()) {
            try {
                int guestId = Integer.parseInt(guestIdStr);
                Guest guest = guestDAO.getGuestById(guestId);

                if (guest == null) {
                    out.println("<div class='alert alert-error'>⚠ Guest not found.</div>");
                } else {
                    List<String[]> history = getGuestReservations(guestId);

                    // Guest profile card
                    out.println("<div class='card guest-profile'>");
                    out.println("<div class='profile-header'>");
                    out.println("<div class='avatar'>" + guest.getFullName().substring(0, 1).toUpperCase() + "</div>");
                    out.println("<div class='profile-info'>");
                    out.println("<h2>" + guest.getFullName() + "</h2>");
                    out.println("<p>📱 " + (guest.getContactNumber() != null ? guest.getContactNumber() : "-") + "</p>");
                    out.println("<p>📧 " + (guest.getEmail() != null && !guest.getEmail().isEmpty() ? guest.getEmail() : "-") + "</p>");
                    out.println("<p>🪪 " + (guest.getNicNumber() != null && !guest.getNicNumber().isEmpty() ? guest.getNicNumber() : "-") + "</p>");
                    out.println("<p>📍 " + (guest.getAddress() != null ? guest.getAddress() : "-") + "</p>");
                    out.println("</div>");

                    // Guest stats
                    int    totalVisits  = history.size();
                    double totalSpent   = history.stream().mapToDouble(r -> {
                        try { return Double.parseDouble(r[5]); } catch (Exception e) { return 0; }
                    }).sum();
                    long completedStays = history.stream().filter(r -> "CHECKED_OUT".equals(r[4])).count();

                    out.println("<div class='profile-stats'>");
                    out.println("<div class='pstat'><div class='pstat-num'>" + totalVisits + "</div><div class='pstat-label'>Total Bookings</div></div>");
                    out.println("<div class='pstat'><div class='pstat-num'>" + completedStays + "</div><div class='pstat-label'>Completed Stays</div></div>");
                    out.println("<div class='pstat'><div class='pstat-num'>LKR " + String.format("%,.0f", totalSpent) + "</div><div class='pstat-label'>Total Spent</div></div>");
                    out.println("</div>");
                    out.println("</div></div>"); // end profile-header + card

                    // Reservation history table
                    out.println("<div class='card'>");
                    out.println("<h2>📋 Reservation History (" + totalVisits + " booking" + (totalVisits != 1 ? "s" : "") + ")</h2>");

                    if (history.isEmpty()) {
                        out.println("<p style='color:#aaa;text-align:center;padding:30px;'>No reservations found for this guest.</p>");
                    } else {
                        out.println("<table><thead><tr>");
                        out.println("<th>Reservation #</th><th>Room</th><th>Check-in</th><th>Check-out</th><th>Nights</th><th>Total (LKR)</th><th>Status</th><th></th>");
                        out.println("</tr></thead><tbody>");
                        for (String[] r : history) {
                            // r: [reservationNumber, roomNumber, checkIn, checkOut, status, totalAmount, roomType, nights]
                            String badgeClass = getBadgeClass(r[4]);
                            out.println("<tr>");
                            out.println("<td><strong>" + r[0] + "</strong></td>");
                            out.println("<td>Room " + r[1] + "<br><small style='color:#aaa;'>" + r[6] + "</small></td>");
                            out.println("<td>" + r[2] + "</td>");
                            out.println("<td>" + r[3] + "</td>");
                            out.println("<td>" + r[7] + "</td>");
                            out.println("<td><strong>" + String.format("%,.2f", Double.parseDouble(r[5])) + "</strong></td>");
                            out.println("<td><span class='badge " + badgeClass + "'>" + r[4] + "</span></td>");
                            out.println("<td><a href='" + contextPath + "/reservation?action=view&reservationNumber=" + r[0] + "' class='view-link'>View →</a></td>");
                            out.println("</tr>");
                        }
                        out.println("</tbody></table>");
                    }
                    out.println("</div>");
                }
            } catch (NumberFormatException e) {
                out.println("<div class='alert alert-error'>⚠ Invalid guest ID.</div>");
            }
        }

        // ---- If no search yet, show all guests ----
        if (searchQuery == null) {
            List<Guest> allGuests = guestDAO.getAllGuests();
            out.println("<div class='card'>");
            out.println("<div style='display:flex;justify-content:space-between;align-items:center;margin-bottom:16px;'>");
            out.println("<h2 style='margin:0;border:none;padding:0;'>All Guests (" + allGuests.size() + ")</h2>");
            out.println("<input class='search-input' type='text' placeholder='🔍 Filter...' oninput='filterTable(this.value)'>");
            out.println("</div>");
            out.println("<table id='guestTable'><thead><tr><th>Full Name</th><th>NIC / Passport</th><th>Contact</th><th>Email</th><th>Action</th></tr></thead><tbody>");
            for (Guest g : allGuests) {
                out.println("<tr>");
                out.println("<td><strong>" + g.getFullName() + "</strong></td>");
                out.println("<td>" + (g.getNicNumber() != null && !g.getNicNumber().isEmpty() ? g.getNicNumber() : "-") + "</td>");
                out.println("<td>" + (g.getContactNumber() != null ? g.getContactNumber() : "-") + "</td>");
                out.println("<td>" + (g.getEmail() != null && !g.getEmail().isEmpty() ? g.getEmail() : "-") + "</td>");
                out.println("<td><a href='" + contextPath + "/guest?action=history&guestId=" + g.getGuestId() + "' class='view-link'>View History →</a></td>");
                out.println("</tr>");
            }
            out.println("</tbody></table></div>");
        }

        out.println("</div>"); // end main

        out.println("<script>");
        out.println("function filterTable(q) {");
        out.println("  document.querySelectorAll('#guestTable tbody tr').forEach(row => {");
        out.println("    row.style.display = row.textContent.toLowerCase().includes(q.toLowerCase()) ? '' : 'none';");
        out.println("  });");
        out.println("}");
        out.println("</script>");

        printFooter(out);
    }

    // ================================================
    // DATA QUERIES
    // ================================================

    private List<Guest> searchGuests(String query) {
        List<Guest> list = new ArrayList<>();
        String sql = "SELECT * FROM guests WHERE full_name LIKE ? OR nic_number LIKE ? ORDER BY full_name";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + query + "%");
            ps.setString(2, "%" + query + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapGuest(rs));
        } catch (SQLException e) {
            System.err.println("[GuestServlet] searchGuests: " + e.getMessage());
        }
        return list;
    }

    private List<String[]> getGuestReservations(int guestId) {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT r.reservation_number, ro.room_number, r.check_in_date, r.check_out_date, " +
                     "r.status, r.total_amount, rt.type_name, " +
                     "DATEDIFF(r.check_out_date, r.check_in_date) as nights " +
                     "FROM reservations r " +
                     "JOIN rooms ro ON r.room_id = ro.room_id " +
                     "JOIN room_types rt ON ro.room_type_id = rt.room_type_id " +
                     "WHERE r.guest_id = ? ORDER BY r.check_in_date DESC";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, guestId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new String[]{
                    rs.getString("reservation_number"),
                    rs.getString("room_number"),
                    rs.getString("check_in_date"),
                    rs.getString("check_out_date"),
                    rs.getString("status"),
                    String.valueOf(rs.getDouble("total_amount")),
                    rs.getString("type_name"),
                    String.valueOf(rs.getInt("nights"))
                });
            }
        } catch (SQLException e) {
            System.err.println("[GuestServlet] getGuestReservations: " + e.getMessage());
        }
        return list;
    }

    private Guest mapGuest(ResultSet rs) throws SQLException {
        Guest g = new Guest();
        g.setGuestId(rs.getInt("guest_id"));
        g.setFullName(rs.getString("full_name"));
        g.setAddress(rs.getString("address"));
        g.setContactNumber(rs.getString("contact_number"));
        g.setEmail(rs.getString("email"));
        g.setNicNumber(rs.getString("nic_number"));
        return g;
    }

    private String getBadgeClass(String status) {
        if (status == null) return "";
        switch (status.toUpperCase()) {
            case "CONFIRMED":   return "badge-confirmed";
            case "CHECKED_IN":  return "badge-checkedin";
            case "CHECKED_OUT": return "badge-checkedout";
            case "CANCELLED":   return "badge-cancelled";
            default:            return "";
        }
    }

    // ================================================
    // HEADER & FOOTER
    // ================================================
    private void printHeader(PrintWriter out, String contextPath, User user) {
        out.println("<!DOCTYPE html><html lang='en'><head>");
        out.println("<meta charset='UTF-8'><meta name='viewport' content='width=device-width,initial-scale=1.0'>");
        out.println("<title>Guest History | Ocean View Resort</title>");
        out.println("<style>");
        out.println("* { margin:0; padding:0; box-sizing:border-box; }");
        out.println("body { font-family:'Segoe UI',sans-serif; background:#f0f4f8; }");
        out.println(".sidebar { width:240px; height:100vh; background:linear-gradient(180deg,#0f4c75,#1b6ca8); position:fixed; top:0; left:0; padding:30px 0; overflow-y:auto; }");
        out.println(".sidebar .logo { text-align:center; padding:0 20px 30px; border-bottom:1px solid rgba(255,255,255,0.15); }");
        out.println(".sidebar .logo h2 { font-size:16px; font-weight:700; color:white; }");
        out.println(".sidebar nav a { display:block; padding:12px 25px; color:rgba(255,255,255,0.85); text-decoration:none; font-size:14px; }");
        out.println(".sidebar nav a:hover,.sidebar nav a.active { background:rgba(255,255,255,0.15); border-left:3px solid white; color:white; }");
        out.println(".sidebar .logout { position:absolute; bottom:20px; width:100%; padding:0 20px; }");
        out.println(".sidebar .logout a { display:block; padding:11px; text-align:center; background:rgba(255,255,255,0.15); color:white; border-radius:8px; text-decoration:none; font-size:13px; }");
        out.println(".sidebar .logout a:hover { background:rgba(255,80,80,0.4); }");
        out.println(".main { margin-left:240px; padding:30px; }");
        out.println(".page-header { margin-bottom:24px; }");
        out.println(".page-header h1 { font-size:22px; color:#1a1a2e; font-weight:700; }");
        out.println(".page-header p  { color:#888; font-size:13px; margin-top:4px; }");
        out.println(".card { background:white; border-radius:14px; padding:24px; box-shadow:0 2px 12px rgba(0,0,0,0.07); margin-bottom:24px; }");
        out.println(".card h2 { font-size:16px; font-weight:700; color:#1a1a2e; margin-bottom:16px; padding-bottom:12px; border-bottom:2px solid #f0f4f8; }");
        out.println(".search-card { padding:20px 24px; }");

        // Guest profile
        out.println(".guest-profile { padding:28px; }");
        out.println(".profile-header { display:flex; gap:24px; align-items:flex-start; flex-wrap:wrap; }");
        out.println(".avatar { width:64px; height:64px; border-radius:50%; background:linear-gradient(135deg,#0f4c75,#1b6ca8); color:white; font-size:26px; font-weight:800; display:flex; align-items:center; justify-content:center; flex-shrink:0; }");
        out.println(".profile-info { flex:1; }");
        out.println(".profile-info h2 { font-size:20px; font-weight:800; color:#1a1a2e; margin-bottom:8px; border:none; padding:0; }");
        out.println(".profile-info p  { font-size:13px; color:#666; margin-bottom:4px; }");
        out.println(".profile-stats { display:flex; gap:20px; flex-wrap:wrap; margin-left:auto; }");
        out.println(".pstat { text-align:center; background:#f0f7ff; border-radius:12px; padding:14px 20px; min-width:100px; }");
        out.println(".pstat-num { font-size:18px; font-weight:800; color:#0f4c75; }");
        out.println(".pstat-label { font-size:11px; color:#888; margin-top:4px; }");

        // Form
        out.println(".form-group { display:flex; flex-direction:column; gap:6px; }");
        out.println(".form-group label { font-size:13px; font-weight:600; color:#444; }");
        out.println(".form-group input { padding:11px 14px; border:1.5px solid #e0e0e0; border-radius:10px; font-size:14px; outline:none; font-family:inherit; }");
        out.println(".form-group input:focus { border-color:#1b6ca8; }");
        out.println(".search-input { padding:9px 16px; border:1.5px solid #e0e0e0; border-radius:10px; font-size:13px; width:220px; outline:none; }");
        out.println(".search-input:focus { border-color:#1b6ca8; }");

        // Table
        out.println("table { width:100%; border-collapse:collapse; }");
        out.println("th { background:#f8fafc; padding:11px 16px; text-align:left; font-size:12px; font-weight:700; color:#666; text-transform:uppercase; border-bottom:2px solid #f0f4f8; }");
        out.println("td { padding:13px 16px; font-size:13px; color:#333; border-bottom:1px solid #f8fafc; vertical-align:middle; }");
        out.println("tr:hover td { background:#fafbff; }");
        out.println(".view-link { color:#1b6ca8; font-weight:600; text-decoration:none; font-size:12px; }");
        out.println(".view-link:hover { text-decoration:underline; }");

        // Badges
        out.println(".badge { padding:4px 10px; border-radius:20px; font-size:11px; font-weight:700; }");
        out.println(".badge-confirmed  { background:#dbeafe; color:#1e40af; }");
        out.println(".badge-checkedin  { background:#d1fae5; color:#065f46; }");
        out.println(".badge-checkedout { background:#f3f4f6; color:#6b7280; }");
        out.println(".badge-cancelled  { background:#fee2e2; color:#991b1b; }");

        // Buttons
        out.println(".btn { padding:10px 20px; border:none; border-radius:10px; font-size:13px; font-weight:600; cursor:pointer; text-decoration:none; display:inline-block; transition:transform 0.2s; }");
        out.println(".btn-primary   { background:linear-gradient(135deg,#0f4c75,#1b6ca8); color:white; }");
        out.println(".btn-secondary { background:#f0f4f8; color:#555; }");
        out.println(".btn:hover { transform:translateY(-2px); }");

        // Alert
        out.println(".alert { padding:12px 18px; border-radius:10px; font-size:13px; margin-bottom:20px; }");
        out.println(".alert-error { background:#fef2f2; color:#dc2626; border:1px solid #fecaca; }");

        out.println("</style></head><body>");

        // Sidebar
        out.println("<div class='sidebar'><div class='logo'><h2>🏨 Ocean View</h2></div><nav>");
        out.println("<a href='" + contextPath + "/dashboard'>🏠 Dashboard</a>");
        out.println("<a href='" + contextPath + "/reservation?action=add'>➕ New Reservation</a>");
        out.println("<a href='" + contextPath + "/reservation?action=list'>📋 All Reservations</a>");
        out.println("<a href='" + contextPath + "/guest?action=history' class='active'>👤 Guest History</a>");
        out.println("<a href='" + contextPath + "/room?action=list'>🛏 Rooms</a>");
        out.println("<a href='" + contextPath + "/service?action=list'>⭐ Services</a>");
        out.println("<a href='" + contextPath + "/bill'>💰 Billing</a>");
        out.println("<a href='" + contextPath + "/report'>📊 Reports</a>");
        out.println("<a href='" + contextPath + "/profile'>👤 My Profile</a>");
        if (user.isAdmin()) out.println("<a href='" + contextPath + "/user?action=list'>👥 Manage Users</a>");
        out.println("<a href='" + contextPath + "/help'>❓ Help & Guide</a>");
        out.println("</nav><div class='logout'><a href='" + contextPath + "/logout'>🚪 Logout</a></div></div>");
    }

    private void printFooter(PrintWriter out) {
        out.println("</body></html>");
    }
}