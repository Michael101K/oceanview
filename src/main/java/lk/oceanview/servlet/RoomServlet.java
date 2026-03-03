package lk.oceanview.servlet;

import lk.oceanview.dao.RoomDAO;
import lk.oceanview.model.Room;
import lk.oceanview.model.User;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * RoomServlet
 * GET  ?action=list             -> show all rooms
 * POST action=updateStatus      -> update room status (Admin only)
 */
public class RoomServlet extends HttpServlet {

    private RoomDAO roomDAO;

    @Override
    public void init() throws ServletException {
        roomDAO = new RoomDAO();
    }

    // ------------------------------------------------
    // GET
    // ------------------------------------------------
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Session check
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedInUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login.html?error=session");
            return;
        }

        showRoomList(request, response, session);
    }

    // ------------------------------------------------
    // POST - update room status (Admin only)
    // ------------------------------------------------
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Session check
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedInUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login.html?error=session");
            return;
        }

        // Only admin can change room status
        User user = (User) session.getAttribute("loggedInUser");
        if (!user.isAdmin()) {
            response.sendRedirect(request.getContextPath() + "/room?action=list&error=unauthorized");
            return;
        }

        String action = request.getParameter("action");
        if ("updateStatus".equals(action)) {
            try {
                int    roomId = Integer.parseInt(request.getParameter("roomId"));
                String status = request.getParameter("status");

                // Validate status value
                if (status != null && (status.equals("AVAILABLE") ||
                                       status.equals("OCCUPIED")  ||
                                       status.equals("MAINTENANCE"))) {
                    roomDAO.updateRoomStatus(roomId, status);
                }
            } catch (Exception e) {
                System.err.println("[RoomServlet] updateStatus error: " + e.getMessage());
            }
        }

        response.sendRedirect(request.getContextPath() + "/room?action=list&msg=updated");
    }

    // ------------------------------------------------
    // Show Room List Page
    // ------------------------------------------------
    private void showRoomList(HttpServletRequest request, HttpServletResponse response,
                              HttpSession session) throws IOException {

        List<Room> rooms   = roomDAO.getAllRooms();
        User user          = (User) session.getAttribute("loggedInUser");
        String contextPath = request.getContextPath();
        String msg         = request.getParameter("msg");
        String error       = request.getParameter("error");

        // Count by status
        long available   = rooms.stream().filter(r -> "AVAILABLE".equals(r.getStatus())).count();
        long occupied    = rooms.stream().filter(r -> "OCCUPIED".equals(r.getStatus())).count();
        long maintenance = rooms.stream().filter(r -> "MAINTENANCE".equals(r.getStatus())).count();

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html><html lang='en'><head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("<title>Rooms | Ocean View Resort</title>");
        out.println("<style>");
        out.println("* { margin:0; padding:0; box-sizing:border-box; }");
        out.println("body { font-family:'Segoe UI',sans-serif; background:#f0f4f8; }");

        // Sidebar
        out.println(".sidebar { width:240px; height:100vh; background:linear-gradient(180deg,#0f4c75,#1b6ca8); position:fixed; top:0; left:0; padding:30px 0; color:white; }");
        out.println(".sidebar .logo { text-align:center; padding:0 20px 30px; border-bottom:1px solid rgba(255,255,255,0.15); }");
        out.println(".sidebar .logo h2 { font-size:16px; font-weight:700; }");
        out.println(".sidebar nav a { display:block; padding:12px 25px; color:rgba(255,255,255,0.85); text-decoration:none; font-size:14px; }");
        out.println(".sidebar nav a:hover, .sidebar nav a.active { background:rgba(255,255,255,0.15); border-left:3px solid white; color:white; }");
        out.println(".sidebar .logout { position:absolute; bottom:20px; width:100%; padding:0 20px; }");
        out.println(".sidebar .logout a { display:block; padding:11px; text-align:center; background:rgba(255,255,255,0.15); color:white; border-radius:8px; text-decoration:none; font-size:13px; }");
        out.println(".sidebar .logout a:hover { background:rgba(255,80,80,0.4); }");

        // Main
        out.println(".main { margin-left:240px; padding:30px; }");
        out.println(".page-header { margin-bottom:24px; }");
        out.println(".page-header h1 { font-size:22px; color:#1a1a2e; font-weight:700; }");
        out.println(".page-header p { color:#888; font-size:13px; margin-top:4px; }");

        // Stats
        out.println(".stats-grid { display:grid; grid-template-columns:repeat(3,1fr); gap:20px; margin-bottom:28px; }");
        out.println(".stat-card { background:white; border-radius:14px; padding:22px 24px; box-shadow:0 2px 12px rgba(0,0,0,0.07); border-left:4px solid; display:flex; justify-content:space-between; align-items:center; }");
        out.println(".stat-card.green  { border-color:#16a085; }");
        out.println(".stat-card.red    { border-color:#e74c3c; }");
        out.println(".stat-card.orange { border-color:#e67e22; }");
        out.println(".stat-card .number { font-size:32px; font-weight:800; color:#1a1a2e; }");
        out.println(".stat-card .label  { font-size:13px; color:#888; margin-top:4px; }");
        out.println(".stat-card .icon   { font-size:32px; }");

        // Room grid
        out.println(".rooms-grid { display:grid; grid-template-columns:repeat(auto-fill,minmax(220px,1fr)); gap:18px; }");
        out.println(".room-card { background:white; border-radius:14px; padding:22px; box-shadow:0 2px 12px rgba(0,0,0,0.07); border-top:4px solid; transition:transform 0.2s; }");
        out.println(".room-card:hover { transform:translateY(-3px); }");
        out.println(".room-card.available   { border-color:#16a085; }");
        out.println(".room-card.occupied    { border-color:#e74c3c; }");
        out.println(".room-card.maintenance { border-color:#e67e22; }");
        out.println(".room-card .room-number { font-size:22px; font-weight:800; color:#1a1a2e; margin-bottom:6px; }");
        out.println(".room-card .room-type   { font-size:13px; color:#666; margin-bottom:4px; }");
        out.println(".room-card .room-rate   { font-size:13px; color:#1b6ca8; font-weight:600; margin-bottom:4px; }");
        out.println(".room-card .room-floor  { font-size:12px; color:#aaa; margin-bottom:14px; }");

        // Badge
        out.println(".badge { padding:4px 12px; border-radius:20px; font-size:11px; font-weight:700; display:inline-block; }");
        out.println(".badge-available    { background:#d1fae5; color:#065f46; }");
        out.println(".badge-occupied     { background:#fee2e2; color:#991b1b; }");
        out.println(".badge-maintenance  { background:#fef3c7; color:#92400e; }");

        // Status dropdown (admin only)
        out.println(".status-form select { width:100%; padding:8px 10px; border:1.5px solid #e0e0e0; border-radius:8px; font-size:12px; margin-top:10px; outline:none; cursor:pointer; }");
        out.println(".status-form select:focus { border-color:#1b6ca8; }");

        // Alert
        out.println(".alert { padding:12px 18px; border-radius:10px; font-size:13px; margin-bottom:20px; }");
        out.println(".alert-success { background:#f0fdf4; color:#16a34a; border:1px solid #bbf7d0; }");
        out.println(".alert-error   { background:#fef2f2; color:#dc2626; border:1px solid #fecaca; }");

        out.println("</style></head><body>");

        // Sidebar
        out.println("<div class='sidebar'>");
        out.println("<div class='logo'><h2>🏨 Ocean View</h2></div><nav>");
        out.println("<a href='" + contextPath + "/dashboard'>🏠 Dashboard</a>");
        out.println("<a href='" + contextPath + "/reservation?action=add'>➕ New Reservation</a>");
        out.println("<a href='" + contextPath + "/reservation?action=list'>📋 All Reservations</a>");
        out.println("<a href='" + contextPath + "/room?action=list' class='active'>🛏 Rooms</a>");
        out.println("<a href='" + contextPath + "/bill'>💰 Billing</a>");
        if (user.isAdmin()) {
            out.println("<a href='" + contextPath + "/user?action=list'>👥 Manage Users</a>");
        }
        out.println("</nav>");
        out.println("<div class='logout'><a href='" + contextPath + "/logout'>🚪 Logout</a></div>");
        out.println("</div>");

        // Main
        out.println("<div class='main'>");
        out.println("<div class='page-header'><h1>🛏 Room Management</h1>");
        out.println("<p>View and manage all rooms at Ocean View Resort</p></div>");

        // Alerts
        if ("updated".equals(msg)) {
            out.println("<div class='alert alert-success'>✅ Room status updated successfully.</div>");
        }
        if ("unauthorized".equals(error)) {
            out.println("<div class='alert alert-error'>⚠ Only administrators can change room status.</div>");
        }

        // Stats row
        out.println("<div class='stats-grid'>");
        out.println("<div class='stat-card green'><div><div class='number'>" + available + "</div><div class='label'>Available</div></div><div class='icon'>✅</div></div>");
        out.println("<div class='stat-card red'><div><div class='number'>" + occupied + "</div><div class='label'>Occupied</div></div><div class='icon'>🔴</div></div>");
        out.println("<div class='stat-card orange'><div><div class='number'>" + maintenance + "</div><div class='label'>Maintenance</div></div><div class='icon'>🔧</div></div>");
        out.println("</div>");

        // Room cards grid
        out.println("<div class='rooms-grid'>");

        for (Room room : rooms) {
            String cardClass  = room.getStatus().toLowerCase();
            String badgeClass = "badge-" + room.getStatus().toLowerCase();
            String icon       = "AVAILABLE".equals(room.getStatus()) ? "✅" :
                                "OCCUPIED".equals(room.getStatus())  ? "🔴" : "🔧";

            out.println("<div class='room-card " + cardClass + "'>");
            out.println("<div class='room-number'>Room " + room.getRoomNumber() + " " + icon + "</div>");
            out.println("<div class='room-type'>" + room.getRoomTypeName() + "</div>");
            out.println("<div class='room-rate'>LKR " + String.format("%,.0f", room.getRatePerNight()) + " / night</div>");
            out.println("<div class='room-floor'>Floor " + room.getFloor() + " &nbsp;|&nbsp; Max " + room.getMaxOccupancy() + " guests</div>");
            out.println("<span class='badge " + badgeClass + "'>" + room.getStatus() + "</span>");

            // Admin status change dropdown
            if (user.isAdmin()) {
                out.println("<form method='POST' action='" + contextPath + "/room' class='status-form'>");
                out.println("<input type='hidden' name='action' value='updateStatus'>");
                out.println("<input type='hidden' name='roomId' value='" + room.getRoomId() + "'>");
                out.println("<select name='status' onchange='this.form.submit()'>");
                out.println("<option value=''>-- Change Status --</option>");
                out.println("<option value='AVAILABLE'"   + ("AVAILABLE".equals(room.getStatus())    ? " selected" : "") + ">✅ Available</option>");
                out.println("<option value='OCCUPIED'"    + ("OCCUPIED".equals(room.getStatus())     ? " selected" : "") + ">🔴 Occupied</option>");
                out.println("<option value='MAINTENANCE'" + ("MAINTENANCE".equals(room.getStatus())  ? " selected" : "") + ">🔧 Maintenance</option>");
                out.println("</select>");
                out.println("</form>");
            }

            out.println("</div>"); // end room-card
        }

        out.println("</div>"); // end rooms-grid
        out.println("</div>"); // end main
        out.println("</body></html>");
    }
}