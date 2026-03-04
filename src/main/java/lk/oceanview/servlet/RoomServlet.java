package lk.oceanview.servlet;

import lk.oceanview.dao.RoomDAO;
import lk.oceanview.model.Room;
import lk.oceanview.model.User;
import lk.oceanview.config.DBConnection;

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
import java.util.List;

/**
 * RoomServlet
 * GET  ?action=list            -> show all rooms + room types (tabbed)
 * POST action=addRoom          -> add a new room
 * POST action=updateRoom       -> edit a room
 * POST action=deleteRoom       -> delete a room
 * POST action=updateStatus     -> change room status
 * POST action=addRoomType      -> add a new room type
 * POST action=updateRoomType   -> edit a room type
 * POST action=deleteRoomType   -> delete a room type
 */
public class RoomServlet extends HttpServlet {

    private RoomDAO roomDAO;

    @Override
    public void init() throws ServletException {
        roomDAO = new RoomDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedInUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login.html?error=session");
            return;
        }
        showRoomPage(request, response, session);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedInUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login.html?error=session");
            return;
        }
        User user          = (User) session.getAttribute("loggedInUser");
        String contextPath = request.getContextPath();
        String action      = request.getParameter("action");

        if (!"updateStatus".equals(action) && !user.isAdmin()) {
            response.sendRedirect(contextPath + "/room?action=list&error=unauthorized");
            return;
        }

        switch (action != null ? action : "") {
            case "addRoom":        addRoom(request, response, contextPath);        break;
            case "updateRoom":     updateRoom(request, response, contextPath);     break;
            case "deleteRoom":     deleteRoom(request, response, contextPath);     break;
            case "updateStatus":   updateStatus(request, response, contextPath);   break;
            case "addRoomType":    addRoomType(request, response, contextPath);    break;
            case "updateRoomType": updateRoomType(request, response, contextPath); break;
            case "deleteRoomType": deleteRoomType(request, response, contextPath); break;
            default: response.sendRedirect(contextPath + "/room?action=list");
        }
    }

    // ================================================
    // MAIN PAGE
    // ================================================
    private void showRoomPage(HttpServletRequest request, HttpServletResponse response,
                              HttpSession session) throws IOException {

        List<Room>     rooms     = roomDAO.getAllRooms();
        List<RoomType> roomTypes = getAllRoomTypes();
        User user                = (User) session.getAttribute("loggedInUser");
        String contextPath       = request.getContextPath();
        String msg               = request.getParameter("msg");
        String error             = request.getParameter("error");
        String tab               = request.getParameter("tab") != null ? request.getParameter("tab") : "rooms";

        long available   = rooms.stream().filter(r -> "AVAILABLE".equals(r.getStatus())).count();
        long occupied    = rooms.stream().filter(r -> "OCCUPIED".equals(r.getStatus())).count();
        long maintenance = rooms.stream().filter(r -> "MAINTENANCE".equals(r.getStatus())).count();

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        printHeader(out, contextPath, user);

        out.println("<div class='main'>");
        out.println("<div class='page-header'>");
        out.println("<div style='display:flex;justify-content:space-between;align-items:center;'>");
        out.println("<div><h1>🛏 Room Management</h1><p>Manage rooms and room types at Ocean View Resort</p></div>");
        if (user.isAdmin()) {
            out.println("<div style='display:flex;gap:10px;'>");
            out.println("<button onclick='openAddRoomModal()' class='btn btn-primary'>➕ Add Room</button>");
            out.println("<button onclick='openAddTypeModal()' class='btn btn-outline'>➕ Add Room Type</button>");
            out.println("</div>");
        }
        out.println("</div></div>");

        // Alerts
        if (msg != null) {
            String msgText = msg.equals("roomAdded")     ? "✅ Room added successfully."        :
                             msg.equals("roomUpdated")   ? "✅ Room updated successfully."      :
                             msg.equals("roomDeleted")   ? "✅ Room deleted successfully."      :
                             msg.equals("statusUpdated") ? "✅ Room status updated."            :
                             msg.equals("typeAdded")     ? "✅ Room type added successfully."   :
                             msg.equals("typeUpdated")   ? "✅ Room type updated successfully." :
                             msg.equals("typeDeleted")   ? "✅ Room type deleted successfully." : "";
            if (!msgText.isEmpty())
                out.println("<div class='alert alert-success'>" + msgText + "</div>");
        }
        if ("unauthorized".equals(error))
            out.println("<div class='alert alert-error'>⚠ Only administrators can perform this action.</div>");
        if ("inuse".equals(error))
            out.println("<div class='alert alert-error'>⚠ Cannot delete — this room type is assigned to existing rooms. Reassign those rooms first.</div>");

        // Stats
        out.println("<div class='stats-grid'>");
        out.println("<div class='stat-card blue'><div><div class='number'>" + rooms.size() + "</div><div class='label'>Total Rooms</div></div><div class='icon'>🛏</div></div>");
        out.println("<div class='stat-card green'><div><div class='number'>" + available + "</div><div class='label'>Available</div></div><div class='icon'>✅</div></div>");
        out.println("<div class='stat-card red'><div><div class='number'>" + occupied + "</div><div class='label'>Occupied</div></div><div class='icon'>🔴</div></div>");
        out.println("<div class='stat-card orange'><div><div class='number'>" + maintenance + "</div><div class='label'>Maintenance</div></div><div class='icon'>🔧</div></div>");
        out.println("</div>");

        // Tabs
        out.println("<div class='tabs'>");
        out.println("<button class='tab-btn " + ("rooms".equals(tab) ? "active" : "") + "' onclick='switchTab(\"rooms\",this)'>🛏 Rooms (" + rooms.size() + ")</button>");
        out.println("<button class='tab-btn " + ("types".equals(tab) ? "active" : "") + "' onclick='switchTab(\"types\",this)'>📂 Room Types (" + roomTypes.size() + ")</button>");
        out.println("</div>");

        // ---- ROOMS TAB ----
        out.println("<div id='tab-rooms' class='tab-content " + ("rooms".equals(tab) ? "" : "hidden") + "'>");
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
            out.println("<form method='POST' action='" + contextPath + "/room' class='status-form'>");
            out.println("<input type='hidden' name='action' value='updateStatus'>");
            out.println("<input type='hidden' name='roomId' value='" + room.getRoomId() + "'>");
            out.println("<select name='status' onchange='this.form.submit()'>");
            out.println("<option value=''>-- Change Status --</option>");
            out.println("<option value='AVAILABLE'"   + ("AVAILABLE".equals(room.getStatus())   ? " selected" : "") + ">✅ Available</option>");
            out.println("<option value='OCCUPIED'"    + ("OCCUPIED".equals(room.getStatus())    ? " selected" : "") + ">🔴 Occupied</option>");
            out.println("<option value='MAINTENANCE'" + ("MAINTENANCE".equals(room.getStatus()) ? " selected" : "") + ">🔧 Maintenance</option>");
            out.println("</select></form>");
            if (user.isAdmin()) {
                out.println("<div class='room-actions'>");
                out.println("<button onclick='openEditRoomModal(" +
                    room.getRoomId() + ",\"" + room.getRoomNumber() + "\"," +
                    room.getRoomTypeId() + "," + room.getFloor() + ",\"" +
                    room.getStatus() + "\")' class='btn-sm btn-edit'>✏ Edit</button>");
                out.println("<form method='POST' action='" + contextPath + "/room' style='display:inline;' " +
                            "onsubmit='return confirm(\"Delete Room " + room.getRoomNumber() + "? This cannot be undone.\")'>");
                out.println("<input type='hidden' name='action' value='deleteRoom'>");
                out.println("<input type='hidden' name='roomId' value='" + room.getRoomId() + "'>");
                out.println("<button type='submit' class='btn-sm btn-danger'>🗑 Delete</button>");
                out.println("</form></div>");
            }
            out.println("</div>");
        }
        if (rooms.isEmpty())
            out.println("<p style='color:#aaa;padding:40px;text-align:center;grid-column:1/-1;'>No rooms found. Click \"Add Room\" to get started.</p>");
        out.println("</div></div>");

        // ---- ROOM TYPES TAB ----
        out.println("<div id='tab-types' class='tab-content " + ("types".equals(tab) ? "" : "hidden") + "'>");
        out.println("<div class='card'><table><thead><tr>");
        out.println("<th>#</th><th>Type Name</th><th>Description</th><th>Rate/Night (LKR)</th><th>Max Occupancy</th>");
        if (user.isAdmin()) out.println("<th>Actions</th>");
        out.println("</tr></thead><tbody>");
        int i = 1;
        for (RoomType rt : roomTypes) {
            out.println("<tr><td>" + i++ + "</td>");
            out.println("<td><strong>" + rt.typeName + "</strong></td>");
            out.println("<td style='color:#666;font-size:12px;'>" + (rt.description != null ? rt.description : "-") + "</td>");
            out.println("<td><strong>LKR " + String.format("%,.2f", rt.ratePerNight) + "</strong></td>");
            out.println("<td>" + rt.maxOccupancy + " guests</td>");
            if (user.isAdmin()) {
                out.println("<td>");
                out.println("<button onclick='openEditTypeModal(" + rt.roomTypeId + ",\"" +
                    escape(rt.typeName) + "\",\"" +
                    escape(rt.description != null ? rt.description : "") + "\"," +
                    rt.ratePerNight + "," + rt.maxOccupancy + ")' class='btn-sm btn-edit'>✏ Edit</button> ");
                out.println("<form method='POST' action='" + contextPath + "/room' style='display:inline;' " +
                    "onsubmit='return confirm(\"Delete room type? Rooms using this type must be reassigned first.\")'>");
                out.println("<input type='hidden' name='action'     value='deleteRoomType'>");
                out.println("<input type='hidden' name='roomTypeId' value='" + rt.roomTypeId + "'>");
                out.println("<button type='submit' class='btn-sm btn-danger'>🗑 Delete</button>");
                out.println("</form></td>");
            }
            out.println("</tr>");
        }
        if (roomTypes.isEmpty())
            out.println("<tr><td colspan='6' style='text-align:center;color:#aaa;padding:30px;'>No room types found.</td></tr>");
        out.println("</tbody></table></div></div>");

        // ================================================
        // MODALS
        // ================================================
        if (user.isAdmin()) {
            StringBuilder typeOpts = new StringBuilder();
            for (RoomType rt : roomTypes)
                typeOpts.append("<option value='").append(rt.roomTypeId).append("'>")
                        .append(rt.typeName).append("</option>");

            // Add Room
            printModal(out, "addRoomModal", "➕ Add New Room",
                "<form method='POST' action='" + contextPath + "/room'>" +
                "<input type='hidden' name='action' value='addRoom'>" +
                "<div class='form-grid'>" +
                "<div class='form-group'><label>Room Number *</label><input type='text' name='roomNumber' placeholder='e.g. 103' required /></div>" +
                "<div class='form-group'><label>Room Type *</label><select name='roomTypeId' required>" + typeOpts + "</select></div>" +
                "<div class='form-group'><label>Floor *</label><input type='number' name='floor' min='1' max='20' placeholder='e.g. 1' required /></div>" +
                "<div class='form-group'><label>Status</label><select name='status'><option value='AVAILABLE'>✅ Available</option><option value='MAINTENANCE'>🔧 Maintenance</option></select></div>" +
                "</div>" +
                "<div style='margin-top:20px;display:flex;gap:10px;'>" +
                "<button type='submit' class='btn btn-primary'>✅ Add Room</button>" +
                "<button type='button' onclick='closeModal(\"addRoomModal\")' class='btn btn-secondary'>Cancel</button>" +
                "</div></form>");

            // Edit Room
            printModal(out, "editRoomModal", "✏ Edit Room",
                "<form method='POST' action='" + contextPath + "/room'>" +
                "<input type='hidden' name='action' value='updateRoom'>" +
                "<input type='hidden' name='roomId' id='editRoomId'>" +
                "<div class='form-grid'>" +
                "<div class='form-group'><label>Room Number *</label><input type='text' name='roomNumber' id='editRoomNumber' required /></div>" +
                "<div class='form-group'><label>Room Type *</label><select name='roomTypeId' id='editRoomTypeId'>" + typeOpts + "</select></div>" +
                "<div class='form-group'><label>Floor *</label><input type='number' name='floor' id='editRoomFloor' min='1' max='20' required /></div>" +
                "<div class='form-group'><label>Status</label><select name='status' id='editRoomStatus'><option value='AVAILABLE'>✅ Available</option><option value='OCCUPIED'>🔴 Occupied</option><option value='MAINTENANCE'>🔧 Maintenance</option></select></div>" +
                "</div>" +
                "<div style='margin-top:20px;display:flex;gap:10px;'>" +
                "<button type='submit' class='btn btn-primary'>💾 Save Changes</button>" +
                "<button type='button' onclick='closeModal(\"editRoomModal\")' class='btn btn-secondary'>Cancel</button>" +
                "</div></form>");

            // Add Room Type
            printModal(out, "addTypeModal", "➕ Add Room Type",
                "<form method='POST' action='" + contextPath + "/room'>" +
                "<input type='hidden' name='action' value='addRoomType'>" +
                "<div class='form-grid'>" +
                "<div class='form-group'><label>Type Name *</label><input type='text' name='typeName' placeholder='e.g. Presidential Suite' required /></div>" +
                "<div class='form-group'><label>Rate Per Night (LKR) *</label><input type='number' name='ratePerNight' min='0' step='0.01' placeholder='e.g. 35000.00' required /></div>" +
                "<div class='form-group'><label>Max Occupancy *</label><input type='number' name='maxOccupancy' min='1' max='10' placeholder='e.g. 2' required /></div>" +
                "<div class='form-group'></div>" +
                "<div class='form-group' style='grid-column:1/-1;'><label>Description</label><textarea name='description' rows='2' style='padding:11px 14px;border:1.5px solid #e0e0e0;border-radius:10px;font-size:14px;width:100%;outline:none;font-family:inherit;resize:vertical;'></textarea></div>" +
                "</div>" +
                "<div style='margin-top:20px;display:flex;gap:10px;'>" +
                "<button type='submit' class='btn btn-primary'>✅ Add Room Type</button>" +
                "<button type='button' onclick='closeModal(\"addTypeModal\")' class='btn btn-secondary'>Cancel</button>" +
                "</div></form>");

            // Edit Room Type
            printModal(out, "editTypeModal", "✏ Edit Room Type",
                "<form method='POST' action='" + contextPath + "/room'>" +
                "<input type='hidden' name='action' value='updateRoomType'>" +
                "<input type='hidden' name='roomTypeId' id='editTypeId'>" +
                "<div class='form-grid'>" +
                "<div class='form-group'><label>Type Name *</label><input type='text' name='typeName' id='editTypeName' required /></div>" +
                "<div class='form-group'><label>Rate Per Night (LKR) *</label><input type='number' name='ratePerNight' id='editTypeRate' min='0' step='0.01' required /></div>" +
                "<div class='form-group'><label>Max Occupancy *</label><input type='number' name='maxOccupancy' id='editTypeOccupancy' min='1' max='10' required /></div>" +
                "<div class='form-group'></div>" +
                "<div class='form-group' style='grid-column:1/-1;'><label>Description</label><textarea name='description' id='editTypeDesc' rows='2' style='padding:11px 14px;border:1.5px solid #e0e0e0;border-radius:10px;font-size:14px;width:100%;outline:none;font-family:inherit;resize:vertical;'></textarea></div>" +
                "</div>" +
                "<div style='margin-top:20px;display:flex;gap:10px;'>" +
                "<button type='submit' class='btn btn-primary'>💾 Save Changes</button>" +
                "<button type='button' onclick='closeModal(\"editTypeModal\")' class='btn btn-secondary'>Cancel</button>" +
                "</div></form>");
        }

        out.println("</div>"); // end main

        // JS
        out.println("<script>");
        out.println("function switchTab(tab, btn) {");
        out.println("  document.querySelectorAll('.tab-content').forEach(t => t.classList.add('hidden'));");
        out.println("  document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));");
        out.println("  document.getElementById('tab-' + tab).classList.remove('hidden');");
        out.println("  btn.classList.add('active');");
        out.println("}");
        out.println("function closeModal(id) { document.getElementById(id).style.display='none'; }");
        out.println("function openAddRoomModal()  { document.getElementById('addRoomModal').style.display='flex'; }");
        out.println("function openAddTypeModal()  { document.getElementById('addTypeModal').style.display='flex'; }");
        out.println("function openEditRoomModal(id,number,typeId,floor,status) {");
        out.println("  document.getElementById('editRoomId').value     = id;");
        out.println("  document.getElementById('editRoomNumber').value = number;");
        out.println("  document.getElementById('editRoomTypeId').value = typeId;");
        out.println("  document.getElementById('editRoomFloor').value  = floor;");
        out.println("  document.getElementById('editRoomStatus').value = status;");
        out.println("  document.getElementById('editRoomModal').style.display='flex';");
        out.println("}");
        out.println("function openEditTypeModal(id,name,desc,rate,occ) {");
        out.println("  document.getElementById('editTypeId').value        = id;");
        out.println("  document.getElementById('editTypeName').value      = name;");
        out.println("  document.getElementById('editTypeDesc').value      = desc;");
        out.println("  document.getElementById('editTypeRate').value      = rate;");
        out.println("  document.getElementById('editTypeOccupancy').value = occ;");
        out.println("  document.getElementById('editTypeModal').style.display='flex';");
        out.println("}");
        out.println("window.onclick = function(e) {");
        out.println("  ['addRoomModal','editRoomModal','addTypeModal','editTypeModal'].forEach(id => {");
        out.println("    const el = document.getElementById(id);");
        out.println("    if (el && e.target===el) el.style.display='none';");
        out.println("  });");
        out.println("};");
        out.println("</script>");

        printFooter(out);
    }

    // ================================================
    // ROOM CRUD
    // ================================================
    private void addRoom(HttpServletRequest request, HttpServletResponse response, String contextPath) throws IOException {
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO rooms (room_number, room_type_id, floor, status) VALUES (?,?,?,?)")) {
            ps.setString(1, request.getParameter("roomNumber"));
            ps.setInt(2, Integer.parseInt(request.getParameter("roomTypeId")));
            ps.setInt(3, Integer.parseInt(request.getParameter("floor")));
            ps.setString(4, request.getParameter("status"));
            ps.executeUpdate();
            response.sendRedirect(contextPath + "/room?action=list&msg=roomAdded");
        } catch (Exception e) {
            System.err.println("[RoomServlet] addRoom: " + e.getMessage());
            response.sendRedirect(contextPath + "/room?action=list&error=save");
        }
    }

    private void updateRoom(HttpServletRequest request, HttpServletResponse response, String contextPath) throws IOException {
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE rooms SET room_number=?,room_type_id=?,floor=?,status=? WHERE room_id=?")) {
            ps.setString(1, request.getParameter("roomNumber"));
            ps.setInt(2, Integer.parseInt(request.getParameter("roomTypeId")));
            ps.setInt(3, Integer.parseInt(request.getParameter("floor")));
            ps.setString(4, request.getParameter("status"));
            ps.setInt(5, Integer.parseInt(request.getParameter("roomId")));
            ps.executeUpdate();
            response.sendRedirect(contextPath + "/room?action=list&msg=roomUpdated");
        } catch (Exception e) {
            System.err.println("[RoomServlet] updateRoom: " + e.getMessage());
            response.sendRedirect(contextPath + "/room?action=list&error=save");
        }
    }

    private void deleteRoom(HttpServletRequest request, HttpServletResponse response, String contextPath) throws IOException {
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM rooms WHERE room_id=?")) {
            ps.setInt(1, Integer.parseInt(request.getParameter("roomId")));
            ps.executeUpdate();
            response.sendRedirect(contextPath + "/room?action=list&msg=roomDeleted");
        } catch (Exception e) {
            System.err.println("[RoomServlet] deleteRoom: " + e.getMessage());
            response.sendRedirect(contextPath + "/room?action=list&error=save");
        }
    }

    private void updateStatus(HttpServletRequest request, HttpServletResponse response, String contextPath) throws IOException {
        try {
            String status = request.getParameter("status");
            if (status != null && (status.equals("AVAILABLE") || status.equals("OCCUPIED") || status.equals("MAINTENANCE")))
                roomDAO.updateRoomStatus(Integer.parseInt(request.getParameter("roomId")), status);
            response.sendRedirect(contextPath + "/room?action=list&msg=statusUpdated");
        } catch (Exception e) {
            response.sendRedirect(contextPath + "/room?action=list");
        }
    }

    // ================================================
    // ROOM TYPE CRUD
    // ================================================
    private void addRoomType(HttpServletRequest request, HttpServletResponse response, String contextPath) throws IOException {
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO room_types (type_name, description, rate_per_night, max_occupancy) VALUES (?,?,?,?)")) {
            ps.setString(1, request.getParameter("typeName"));
            ps.setString(2, request.getParameter("description"));
            ps.setDouble(3, Double.parseDouble(request.getParameter("ratePerNight")));
            ps.setInt(4, Integer.parseInt(request.getParameter("maxOccupancy")));
            ps.executeUpdate();
            response.sendRedirect(contextPath + "/room?action=list&tab=types&msg=typeAdded");
        } catch (Exception e) {
            System.err.println("[RoomServlet] addRoomType: " + e.getMessage());
            response.sendRedirect(contextPath + "/room?action=list&tab=types&error=save");
        }
    }

    private void updateRoomType(HttpServletRequest request, HttpServletResponse response, String contextPath) throws IOException {
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE room_types SET type_name=?,description=?,rate_per_night=?,max_occupancy=? WHERE room_type_id=?")) {
            ps.setString(1, request.getParameter("typeName"));
            ps.setString(2, request.getParameter("description"));
            ps.setDouble(3, Double.parseDouble(request.getParameter("ratePerNight")));
            ps.setInt(4, Integer.parseInt(request.getParameter("maxOccupancy")));
            ps.setInt(5, Integer.parseInt(request.getParameter("roomTypeId")));
            ps.executeUpdate();
            response.sendRedirect(contextPath + "/room?action=list&tab=types&msg=typeUpdated");
        } catch (Exception e) {
            System.err.println("[RoomServlet] updateRoomType: " + e.getMessage());
            response.sendRedirect(contextPath + "/room?action=list&tab=types&error=save");
        }
    }

    private void deleteRoomType(HttpServletRequest request, HttpServletResponse response, String contextPath) throws IOException {
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM room_types WHERE room_type_id=?")) {
            ps.setInt(1, Integer.parseInt(request.getParameter("roomTypeId")));
            ps.executeUpdate();
            response.sendRedirect(contextPath + "/room?action=list&tab=types&msg=typeDeleted");
        } catch (Exception e) {
            System.err.println("[RoomServlet] deleteRoomType: " + e.getMessage());
            response.sendRedirect(contextPath + "/room?action=list&tab=types&error=inuse");
        }
    }

    // ================================================
    // HELPERS
    // ================================================
    private List<RoomType> getAllRoomTypes() {
        List<RoomType> list = new ArrayList<>();
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM room_types ORDER BY type_name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                RoomType rt     = new RoomType();
                rt.roomTypeId   = rs.getInt("room_type_id");
                rt.typeName     = rs.getString("type_name");
                rt.description  = rs.getString("description");
                rt.ratePerNight = rs.getDouble("rate_per_night");
                rt.maxOccupancy = rs.getInt("max_occupancy");
                list.add(rt);
            }
        } catch (SQLException e) {
            System.err.println("[RoomServlet] getAllRoomTypes: " + e.getMessage());
        }
        return list;
    }

    private static class RoomType {
        int roomTypeId; String typeName; String description;
        double ratePerNight; int maxOccupancy;
    }

    private String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "&quot;").replace("'", "&#39;");
    }

    private void printModal(PrintWriter out, String id, String title, String body) {
        out.println("<div id='" + id + "' class='modal' style='display:none;'>");
        out.println("<div class='modal-content'>");
        out.println("<div class='modal-header'><h3>" + title + "</h3>");
        out.println("<span onclick='closeModal(\"" + id + "\")' class='close-btn'>✕</span></div>");
        out.println(body);
        out.println("</div></div>");
    }

    // ================================================
    // HEADER & FOOTER
    // ================================================
    private void printHeader(PrintWriter out, String contextPath, User user) {
        out.println("<!DOCTYPE html><html lang='en'><head>");
        out.println("<meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("<title>Rooms | Ocean View Resort</title>");
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
        out.println(".stats-grid { display:grid; grid-template-columns:repeat(4,1fr); gap:20px; margin-bottom:24px; }");
        out.println(".stat-card { background:white; border-radius:14px; padding:20px 24px; box-shadow:0 2px 12px rgba(0,0,0,0.07); border-left:4px solid; display:flex; justify-content:space-between; align-items:center; }");
        out.println(".stat-card.blue{border-color:#1b6ca8;}.stat-card.green{border-color:#16a085;}.stat-card.red{border-color:#e74c3c;}.stat-card.orange{border-color:#e67e22;}");
        out.println(".stat-card .number{font-size:30px;font-weight:800;color:#1a1a2e;}.stat-card .label{font-size:12px;color:#888;margin-top:4px;}.stat-card .icon{font-size:28px;}");
        out.println(".tabs { display:flex; gap:4px; margin-bottom:20px; background:white; padding:6px; border-radius:12px; width:fit-content; box-shadow:0 2px 8px rgba(0,0,0,0.06); }");
        out.println(".tab-btn { padding:9px 24px; border:none; border-radius:9px; font-size:13px; font-weight:600; cursor:pointer; background:transparent; color:#666; transition:all 0.2s; }");
        out.println(".tab-btn.active { background:linear-gradient(135deg,#0f4c75,#1b6ca8); color:white; box-shadow:0 3px 10px rgba(27,108,168,0.3); }");
        out.println(".tab-content.hidden { display:none; }");
        out.println(".rooms-grid { display:grid; grid-template-columns:repeat(auto-fill,minmax(220px,1fr)); gap:18px; }");
        out.println(".room-card { background:white; border-radius:14px; padding:20px; box-shadow:0 2px 12px rgba(0,0,0,0.07); border-top:4px solid; transition:transform 0.2s; }");
        out.println(".room-card:hover{transform:translateY(-3px);}");
        out.println(".room-card.available{border-color:#16a085;}.room-card.occupied{border-color:#e74c3c;}.room-card.maintenance{border-color:#e67e22;}");
        out.println(".room-number{font-size:20px;font-weight:800;color:#1a1a2e;margin-bottom:5px;}");
        out.println(".room-type{font-size:13px;color:#666;margin-bottom:3px;}.room-rate{font-size:13px;color:#1b6ca8;font-weight:600;margin-bottom:3px;}.room-floor{font-size:12px;color:#aaa;margin-bottom:12px;}");
        out.println(".room-actions{display:flex;gap:6px;margin-top:8px;}");
        out.println(".status-form select{width:100%;padding:7px 10px;border:1.5px solid #e0e0e0;border-radius:8px;font-size:12px;margin-top:8px;outline:none;cursor:pointer;}");
        out.println(".card{background:white;border-radius:14px;padding:24px;box-shadow:0 2px 12px rgba(0,0,0,0.07);}");
        out.println("table{width:100%;border-collapse:collapse;}");
        out.println("th{background:#f8fafc;padding:11px 16px;text-align:left;font-size:12px;font-weight:700;color:#666;text-transform:uppercase;border-bottom:2px solid #f0f4f8;}");
        out.println("td{padding:13px 16px;font-size:13px;color:#333;border-bottom:1px solid #f8fafc;vertical-align:middle;}");
        out.println("tr:hover td{background:#fafbff;}");
        out.println(".badge{padding:4px 10px;border-radius:20px;font-size:11px;font-weight:700;}");
        out.println(".badge-available{background:#d1fae5;color:#065f46;}.badge-occupied{background:#fee2e2;color:#991b1b;}.badge-maintenance{background:#fef3c7;color:#92400e;}");
        out.println(".btn{padding:10px 20px;border:none;border-radius:10px;font-size:13px;font-weight:600;cursor:pointer;text-decoration:none;display:inline-block;transition:transform 0.2s;}");
        out.println(".btn-primary{background:linear-gradient(135deg,#0f4c75,#1b6ca8);color:white;}");
        out.println(".btn-outline{background:white;color:#1b6ca8;border:1.5px solid #1b6ca8;}");
        out.println(".btn-secondary{background:#f0f4f8;color:#555;}.btn:hover{transform:translateY(-2px);}");
        out.println(".btn-sm{padding:5px 11px;border:none;border-radius:7px;font-size:11px;font-weight:600;cursor:pointer;}");
        out.println(".btn-edit{background:#ede9fe;color:#5b21b6;}.btn-danger{background:#fee2e2;color:#991b1b;}");
        out.println(".form-grid{display:grid;grid-template-columns:1fr 1fr;gap:16px;}");
        out.println(".form-group{display:flex;flex-direction:column;gap:6px;}");
        out.println(".form-group label{font-size:13px;font-weight:600;color:#444;}");
        out.println(".form-group input,.form-group select{padding:11px 14px;border:1.5px solid #e0e0e0;border-radius:10px;font-size:14px;color:#333;outline:none;font-family:inherit;}");
        out.println(".form-group input:focus,.form-group select:focus{border-color:#1b6ca8;}");
        out.println(".alert{padding:12px 18px;border-radius:10px;font-size:13px;margin-bottom:20px;}");
        out.println(".alert-success{background:#f0fdf4;color:#16a34a;border:1px solid #bbf7d0;}");
        out.println(".alert-error{background:#fef2f2;color:#dc2626;border:1px solid #fecaca;}");
        out.println(".modal{position:fixed;top:0;left:0;width:100%;height:100%;background:rgba(0,0,0,0.5);display:flex;align-items:center;justify-content:center;z-index:1000;}");
        out.println(".modal-content{background:white;border-radius:16px;padding:32px;width:520px;max-width:90%;max-height:90vh;overflow-y:auto;box-shadow:0 20px 60px rgba(0,0,0,0.3);}");
        out.println(".modal-header{display:flex;justify-content:space-between;align-items:center;margin-bottom:24px;padding-bottom:14px;border-bottom:2px solid #f0f4f8;}");
        out.println(".modal-header h3{font-size:17px;font-weight:700;color:#1a1a2e;}");
        out.println(".close-btn{cursor:pointer;font-size:20px;color:#888;}.close-btn:hover{color:#333;}");
        out.println("</style></head><body>");
        out.println("<div class='sidebar'><div class='logo'><h2>🏨 Ocean View</h2></div><nav>");
        out.println("<a href='" + contextPath + "/dashboard'>🏠 Dashboard</a>");
        out.println("<a href='" + contextPath + "/reservation?action=add'>➕ New Reservation</a>");
        out.println("<a href='" + contextPath + "/reservation?action=list'>📋 All Reservations</a>");
        out.println("<a href='" + contextPath + "/room?action=list' class='active'>🛏 Rooms</a>");
        out.println("<a href='" + contextPath + "/service?action=list'>⭐ Services</a>");
        out.println("<a href='" + contextPath + "/bill'>💰 Billing</a>");
        out.println("<a href='" + contextPath + "/help' class='active'>❓ Help & Guide</a>");
        if (user.isAdmin()) out.println("<a href='" + contextPath + "/user?action=list'>👥 Manage Users</a>");
        out.println("</nav><div class='logout'><a href='" + contextPath + "/logout'>🚪 Logout</a></div></div>");
    }

    private void printFooter(PrintWriter out) {
        out.println("</body></html>");
    }
}