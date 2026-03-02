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

/**
 * DashboardServlet
 * Landing page after successful login.
 * Shows a summary: total rooms, today's check-ins, etc.
 */
public class DashboardServlet extends HttpServlet {

    private ReservationDAO reservationDAO;
    private RoomDAO roomDAO;

    @Override
    public void init() throws ServletException {
        reservationDAO = new ReservationDAO();
        roomDAO        = new RoomDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Session check - redirect to login if not logged in
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedInUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login.html?error=session");
            return;
        }

        // Get logged in user details
        User user         = (User) session.getAttribute("loggedInUser");
        String fullName   = user.getFullName();
        String role       = user.getRole();

        // Fetch summary data
        int totalRooms       = roomDAO.getAllRooms().size();
        int availableRooms   = roomDAO.getAvailableRooms().size();
        int todayCheckIns    = reservationDAO.getTodayCheckIns().size();
        int confirmedCount   = reservationDAO.getReservationsByStatus("CONFIRMED").size();

        String contextPath = request.getContextPath();

        // Build dashboard HTML
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html lang='en'><head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("<title>Dashboard | Ocean View Resort</title>");
        out.println("<style>");
        out.println("* { margin:0; padding:0; box-sizing:border-box; }");
        out.println("body { font-family: 'Segoe UI', sans-serif; background:#f0f4f8; }");

        // Sidebar
        out.println(".sidebar { width:240px; height:100vh; background:linear-gradient(180deg,#0f4c75,#1b6ca8); position:fixed; top:0; left:0; padding:30px 0; color:white; }");
        out.println(".sidebar .logo { text-align:center; padding:0 20px 30px; border-bottom:1px solid rgba(255,255,255,0.15); }");
        out.println(".sidebar .logo h2 { font-size:16px; font-weight:700; }");
        out.println(".sidebar .logo p { font-size:11px; opacity:0.7; margin-top:4px; }");
        out.println(".sidebar nav { padding:20px 0; }");
        out.println(".sidebar nav a { display:block; padding:12px 25px; color:rgba(255,255,255,0.85); text-decoration:none; font-size:14px; transition:background 0.2s; }");
        out.println(".sidebar nav a:hover, .sidebar nav a.active { background:rgba(255,255,255,0.15); color:white; border-left:3px solid white; }");
        out.println(".sidebar .logout { position:absolute; bottom:20px; width:100%; padding:0 20px; }");
        out.println(".sidebar .logout a { display:block; padding:11px; text-align:center; background:rgba(255,255,255,0.15); color:white; border-radius:8px; text-decoration:none; font-size:13px; }");
        out.println(".sidebar .logout a:hover { background:rgba(255,80,80,0.4); }");

        // Main content
        out.println(".main { margin-left:240px; padding:30px; }");
        out.println(".topbar { display:flex; justify-content:space-between; align-items:center; margin-bottom:30px; }");
        out.println(".topbar h1 { font-size:22px; color:#1a1a2e; font-weight:700; }");
        out.println(".topbar .user-info { font-size:13px; color:#666; text-align:right; }");
        out.println(".topbar .user-info span { display:block; font-weight:600; color:#1b6ca8; font-size:14px; }");

        // Stat cards
        out.println(".stats-grid { display:grid; grid-template-columns:repeat(4,1fr); gap:20px; margin-bottom:30px; }");
        out.println(".stat-card { background:white; border-radius:14px; padding:24px; box-shadow:0 2px 12px rgba(0,0,0,0.07); border-left:4px solid; }");
        out.println(".stat-card.blue { border-color:#1b6ca8; } .stat-card.green { border-color:#16a085; } .stat-card.orange { border-color:#e67e22; } .stat-card.purple { border-color:#8e44ad; }");
        out.println(".stat-card .number { font-size:36px; font-weight:800; color:#1a1a2e; }");
        out.println(".stat-card .label { font-size:13px; color:#888; margin-top:4px; }");
        out.println(".stat-card .icon { font-size:28px; float:right; margin-top:-5px; }");

        // Quick actions
        out.println(".actions-grid { display:grid; grid-template-columns:repeat(3,1fr); gap:20px; }");
        out.println(".action-card { background:white; border-radius:14px; padding:28px 24px; box-shadow:0 2px 12px rgba(0,0,0,0.07); text-align:center; text-decoration:none; color:#1a1a2e; transition:transform 0.2s,box-shadow 0.2s; }");
        out.println(".action-card:hover { transform:translateY(-4px); box-shadow:0 8px 25px rgba(0,0,0,0.12); }");
        out.println(".action-card .icon { font-size:40px; margin-bottom:12px; }");
        out.println(".action-card h3 { font-size:15px; font-weight:600; margin-bottom:6px; }");
        out.println(".action-card p { font-size:12px; color:#999; }");
        out.println(".section-title { font-size:16px; font-weight:700; color:#1a1a2e; margin-bottom:16px; }");
        out.println("</style></head><body>");

        // Sidebar
        out.println("<div class='sidebar'>");
        out.println("<div class='logo'><h2>🏨 Ocean View</h2><p>Resort Management</p></div>");
        out.println("<nav>");
        out.println("<a href='" + contextPath + "/dashboard' class='active'>🏠 Dashboard</a>");
        out.println("<a href='" + contextPath + "/reservation?action=add'>➕ New Reservation</a>");
        out.println("<a href='" + contextPath + "/reservation?action=list'>📋 All Reservations</a>");
        out.println("<a href='" + contextPath + "/room?action=list'>🛏 Rooms</a>");
        out.println("<a href='" + contextPath + "/bill'>💰 Billing</a>");
        if ("ADMIN".equals(role)) {
            out.println("<a href='" + contextPath + "/user?action=list'>👥 Manage Users</a>");
        }
        out.println("</nav>");
        out.println("<div class='logout'><a href='" + contextPath + "/logout'>🚪 Logout</a></div>");
        out.println("</div>");

        // Main content
        out.println("<div class='main'>");

        // Topbar
        out.println("<div class='topbar'>");
        out.println("<h1>Dashboard</h1>");
        out.println("<div class='user-info'>Welcome back,<span>" + fullName + " (" + role + ")</span></div>");
        out.println("</div>");

        // Stats
        out.println("<div class='stats-grid'>");
        out.println("<div class='stat-card blue'><span class='icon'>🛏</span><div class='number'>" + totalRooms + "</div><div class='label'>Total Rooms</div></div>");
        out.println("<div class='stat-card green'><span class='icon'>✅</span><div class='number'>" + availableRooms + "</div><div class='label'>Available Rooms</div></div>");
        out.println("<div class='stat-card orange'><span class='icon'>📅</span><div class='number'>" + todayCheckIns + "</div><div class='label'>Today's Check-ins</div></div>");
        out.println("<div class='stat-card purple'><span class='icon'>📋</span><div class='number'>" + confirmedCount + "</div><div class='label'>Confirmed Bookings</div></div>");
        out.println("</div>");

        // Quick actions
        out.println("<p class='section-title'>Quick Actions</p>");
        out.println("<div class='actions-grid'>");
        out.println("<a href='" + contextPath + "/reservation?action=add' class='action-card'><div class='icon'>➕</div><h3>New Reservation</h3><p>Add a new guest booking</p></a>");
        out.println("<a href='" + contextPath + "/reservation?action=list' class='action-card'><div class='icon'>🔍</div><h3>Find Reservation</h3><p>Search by reservation number</p></a>");
        out.println("<a href='" + contextPath + "/bill' class='action-card'><div class='icon'>🧾</div><h3>Generate Bill</h3><p>Calculate and print guest bill</p></a>");
        out.println("</div>");

        out.println("</div></body></html>");
    }
}