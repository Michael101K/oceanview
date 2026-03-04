package lk.oceanview.servlet;

import lk.oceanview.dao.ReservationDAO;
import lk.oceanview.dao.RoomDAO;
import lk.oceanview.model.User;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * DashboardServlet
 * GET -> show main dashboard with stats and quick actions
 */
public class DashboardServlet extends HttpServlet {

    private RoomDAO        roomDAO;
    private ReservationDAO reservationDAO;

    @Override
    public void init() throws ServletException {
        roomDAO        = new RoomDAO();
        reservationDAO = new ReservationDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedInUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login.html?error=session");
            return;
        }

        User   user        = (User) session.getAttribute("loggedInUser");
        String contextPath = request.getContextPath();
        String role        = user.getRole();

        // Fetch stats
        int totalRooms     = roomDAO.getAllRooms().size();
        int availableRooms = roomDAO.getAvailableRooms().size();
        int todayCheckIns  = reservationDAO.getTodayCheckIns().size();
        int confirmedCount  = reservationDAO.getReservationsByStatus("CONFIRMED").size();

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy"));

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html><html lang='en'><head>");
        out.println("<meta charset='UTF-8'><meta name='viewport' content='width=device-width,initial-scale=1.0'>");
        out.println("<title>Dashboard | Ocean View Resort</title>");
        out.println("<style>");
        out.println("* { margin:0; padding:0; box-sizing:border-box; }");
        out.println("body { font-family:'Segoe UI',sans-serif; background:#f0f4f8; }");
        out.println(".sidebar { width:240px; height:100vh; background:linear-gradient(180deg,#0f4c75,#1b6ca8); position:fixed; top:0; left:0; padding:30px 0; overflow-y:auto; color:white; }");
        out.println(".sidebar .logo { text-align:center; padding:0 20px 30px; border-bottom:1px solid rgba(255,255,255,0.15); }");
        out.println(".sidebar .logo h2 { font-size:16px; font-weight:700; }");
        out.println(".sidebar .logo p  { font-size:11px; opacity:0.7; margin-top:4px; }");
        out.println(".sidebar nav a { display:block; padding:12px 25px; color:rgba(255,255,255,0.85); text-decoration:none; font-size:14px; }");
        out.println(".sidebar nav a:hover,.sidebar nav a.active { background:rgba(255,255,255,0.15); border-left:3px solid white; color:white; }");
        out.println(".sidebar .logout { position:absolute; bottom:20px; width:100%; padding:0 20px; }");
        out.println(".sidebar .logout a { display:block; padding:11px; text-align:center; background:rgba(255,255,255,0.15); color:white; border-radius:8px; text-decoration:none; font-size:13px; }");
        out.println(".sidebar .logout a:hover { background:rgba(255,80,80,0.4); }");
        out.println(".main { margin-left:240px; padding:30px; }");
        out.println(".topbar { display:flex; justify-content:space-between; align-items:center; margin-bottom:28px; }");
        out.println(".topbar h1 { font-size:22px; color:#1a1a2e; font-weight:700; }");
        out.println(".topbar .date { font-size:13px; color:#888; background:white; padding:8px 16px; border-radius:20px; box-shadow:0 2px 8px rgba(0,0,0,0.06); }");
        out.println(".welcome { font-size:14px; color:#666; margin-top:4px; }");
        out.println(".stats-grid { display:grid; grid-template-columns:repeat(4,1fr); gap:20px; margin-bottom:28px; }");
        out.println(".stat-card { background:white; border-radius:16px; padding:24px; box-shadow:0 2px 12px rgba(0,0,0,0.07); border-left:4px solid; display:flex; justify-content:space-between; align-items:center; transition:transform 0.2s; }");
        out.println(".stat-card:hover { transform:translateY(-3px); }");
        out.println(".stat-card.blue{border-color:#1b6ca8;} .stat-card.green{border-color:#16a085;} .stat-card.orange{border-color:#e67e22;} .stat-card.purple{border-color:#8e44ad;}");
        out.println(".stat-card .number { font-size:34px; font-weight:800; color:#1a1a2e; }");
        out.println(".stat-card .label  { font-size:13px; color:#888; margin-top:4px; }");
        out.println(".stat-card .icon   { font-size:36px; }");
        out.println(".section-title { font-size:16px; font-weight:700; color:#1a1a2e; margin-bottom:16px; }");
        out.println(".actions-grid { display:grid; grid-template-columns:repeat(auto-fill,minmax(180px,1fr)); gap:16px; margin-bottom:28px; }");
        out.println(".action-card { background:white; border-radius:14px; padding:22px; box-shadow:0 2px 12px rgba(0,0,0,0.07); text-decoration:none; text-align:center; transition:transform 0.2s,box-shadow 0.2s; }");
        out.println(".action-card:hover { transform:translateY(-4px); box-shadow:0 8px 24px rgba(0,0,0,0.12); }");
        out.println(".action-card .a-icon  { font-size:32px; margin-bottom:10px; }");
        out.println(".action-card .a-title { font-size:14px; font-weight:700; color:#1a1a2e; }");
        out.println(".action-card .a-desc  { font-size:12px; color:#888; margin-top:4px; }");
        out.println(".info-banner { background:linear-gradient(135deg,#0f4c75,#1b6ca8); border-radius:16px; padding:24px 28px; color:white; margin-bottom:28px; display:flex; justify-content:space-between; align-items:center; }");
        out.println(".info-banner h2 { font-size:18px; font-weight:700; margin-bottom:6px; }");
        out.println(".info-banner p  { font-size:13px; opacity:0.85; }");
        out.println(".info-banner .big-icon { font-size:48px; }");
        out.println("</style></head><body>");

        // Sidebar
        out.println("<div class='sidebar'>");
        out.println("<div class='logo'><h2>🏨 Ocean View Resort</h2><p>Management System</p></div><nav>");
        out.println("<a href='" + contextPath + "/dashboard' class='active'>🏠 Dashboard</a>");
        out.println("<a href='" + contextPath + "/reservation?action=add'>➕ New Reservation</a>");
        out.println("<a href='" + contextPath + "/reservation?action=list'>📋 All Reservations</a>");
        out.println("<a href='" + contextPath + "/guest?action=history'>👤 Guest History</a>");
        out.println("<a href='" + contextPath + "/room?action=list'>🛏 Rooms</a>");
        out.println("<a href='" + contextPath + "/service?action=list'>⭐ Services</a>");
        out.println("<a href='" + contextPath + "/bill'>💰 Billing</a>");
        out.println("<a href='" + contextPath + "/report'>📊 Reports</a>");
        out.println("<a href='" + contextPath + "/profile'>👤 My Profile</a>");
        if ("ADMIN".equals(role)) {
            out.println("<a href='" + contextPath + "/user?action=list'>👥 Manage Users</a>");
        }
        out.println("<a href='" + contextPath + "/help'>❓ Help & Guide</a>");
        out.println("</nav><div class='logout'><a href='" + contextPath + "/logout'>🚪 Logout</a></div></div>");

        // Main
        out.println("<div class='main'>");
        out.println("<div class='topbar'>");
        out.println("<div><h1>🏠 Dashboard</h1><p class='welcome'>Welcome back, <strong>" + user.getFullName() + "</strong> &nbsp;<span style='background:#ede9fe;color:#5b21b6;padding:2px 10px;border-radius:20px;font-size:11px;font-weight:700;'>" + role + "</span></p></div>");
        out.println("<div class='date'>📅 " + today + "</div></div>");

        // Banner
        out.println("<div class='info-banner'><div><h2>Ocean View Resort</h2><p>Galle, Sri Lanka &nbsp;|&nbsp; Reservations, Rooms, Billing & More</p></div><div class='big-icon'>🌊</div></div>");

        // Stats
        out.println("<div class='stats-grid'>");
        out.println("<div class='stat-card blue'><div><div class='number'>" + totalRooms + "</div><div class='label'>Total Rooms</div></div><div class='icon'>🛏</div></div>");
        out.println("<div class='stat-card green'><div><div class='number'>" + availableRooms + "</div><div class='label'>Available Rooms</div></div><div class='icon'>✅</div></div>");
        out.println("<div class='stat-card orange'><div><div class='number'>" + todayCheckIns + "</div><div class='label'>Today's Check-ins</div></div><div class='icon'>🏨</div></div>");
        out.println("<div class='stat-card purple'><div><div class='number'>" + confirmedCount + "</div><div class='label'>Confirmed Bookings</div></div><div class='icon'>📋</div></div>");
        out.println("</div>");

        // Quick actions
        out.println("<p class='section-title'>⚡ Quick Actions</p>");
        out.println("<div class='actions-grid'>");
        out.println("<a href='" + contextPath + "/reservation?action=add' class='action-card'><div class='a-icon'>➕</div><div class='a-title'>New Reservation</div><div class='a-desc'>Book a room for a guest</div></a>");
        out.println("<a href='" + contextPath + "/reservation?action=list' class='action-card'><div class='a-icon'>📋</div><div class='a-title'>All Reservations</div><div class='a-desc'>View and manage bookings</div></a>");
        out.println("<a href='" + contextPath + "/guest?action=history' class='action-card'><div class='a-icon'>👤</div><div class='a-title'>Guest History</div><div class='a-desc'>Look up guest records</div></a>");
        out.println("<a href='" + contextPath + "/bill' class='action-card'><div class='a-icon'>🧾</div><div class='a-title'>Generate Bill</div><div class='a-desc'>Create guest invoices</div></a>");
        out.println("<a href='" + contextPath + "/room?action=list' class='action-card'><div class='a-icon'>🛏</div><div class='a-title'>Room Status</div><div class='a-desc'>View room availability</div></a>");
        out.println("<a href='" + contextPath + "/report' class='action-card'><div class='a-icon'>📊</div><div class='a-title'>Reports</div><div class='a-desc'>Revenue & occupancy</div></a>");
        out.println("<a href='" + contextPath + "/service?action=list' class='action-card'><div class='a-icon'>⭐</div><div class='a-title'>Services</div><div class='a-desc'>Manage resort services</div></a>");
        out.println("<a href='" + contextPath + "/help' class='action-card'><div class='a-icon'>❓</div><div class='a-title'>Help & Guide</div><div class='a-desc'>System user guide</div></a>");
        out.println("</div>");

        out.println("</div></body></html>");
    }
}