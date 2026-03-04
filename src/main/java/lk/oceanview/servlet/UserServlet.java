package lk.oceanview.servlet;

import lk.oceanview.dao.UserDAO;
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
 * UserServlet
 * GET  ?action=list   -> show all users
 * GET  ?action=add    -> show add user form
 * POST action=add     -> save new user
 * POST action=update  -> update user details / toggle active status
 */
public class UserServlet extends HttpServlet {

    private UserDAO userDAO;

    @Override
    public void init() throws ServletException {
        userDAO = new UserDAO();
    }

    // ------------------------------------------------
    // Session + Admin check helper
    // ------------------------------------------------
    private boolean isAdminLoggedIn(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedInUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login.html?error=session");
            return false;
        }
        User user = (User) session.getAttribute("loggedInUser");
        if (!user.isAdmin()) {
            response.sendRedirect(request.getContextPath() + "/dashboard?error=unauthorized");
            return false;
        }
        return true;
    }

    // ------------------------------------------------
    // GET
    // ------------------------------------------------
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!isAdminLoggedIn(request, response)) return;

        String action = request.getParameter("action");
        if (action == null) action = "list";

        switch (action) {
            case "add":
                showAddUserForm(request, response);
                break;
            case "list":
            default:
                showUserList(request, response);
                break;
        }
    }

    // ------------------------------------------------
    // POST
    // ------------------------------------------------
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!isAdminLoggedIn(request, response)) return;

        String action      = request.getParameter("action");
        String contextPath = request.getContextPath();

        switch (action != null ? action : "") {
            case "add":
                addUser(request, response, contextPath);
                break;
            case "update":
                updateUser(request, response, contextPath);
                break;
            default:
                response.sendRedirect(contextPath + "/user?action=list");
        }
    }

    // ------------------------------------------------
    // Show User List
    // ------------------------------------------------
    private void showUserList(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        List<User> users   = userDAO.getAllUsers();
        String contextPath = request.getContextPath();
        String msg         = request.getParameter("msg");
        String error       = request.getParameter("error");

        // Count stats
        long adminCount  = users.stream().filter(u -> "ADMIN".equals(u.getRole())).count();
        long receptionCount = users.stream().filter(u -> "RECEPTIONIST".equals(u.getRole())).count();
        long activeCount = users.stream().filter(User::isActive).count();

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        printHeader(out, "User Management | Ocean View Resort", contextPath);

        out.println("<div class='main'>");
        out.println("<div class='page-header'>");
        out.println("<div style='display:flex;justify-content:space-between;align-items:center;'>");
        out.println("<div><h1>👥 User Management</h1><p>Manage staff accounts and access levels</p></div>");
        out.println("<a href='" + contextPath + "/user?action=add' class='btn btn-primary'>➕ Add New User</a>");
        out.println("</div></div>");

        // Alerts
        if ("added".equals(msg)) {
            out.println("<div class='alert alert-success'>✅ New user added successfully.</div>");
        } else if ("updated".equals(msg)) {
            out.println("<div class='alert alert-success'>✅ User updated successfully.</div>");
        } else if ("exists".equals(error)) {
            out.println("<div class='alert alert-error'>⚠ Username already exists. Please choose a different one.</div>");
        }

        // Stats
        out.println("<div class='stats-grid'>");
        out.println("<div class='stat-card blue'><div><div class='number'>" + users.size() + "</div><div class='label'>Total Staff</div></div><div class='icon'>👥</div></div>");
        out.println("<div class='stat-card green'><div><div class='number'>" + activeCount + "</div><div class='label'>Active Accounts</div></div><div class='icon'>✅</div></div>");
        out.println("<div class='stat-card purple'><div><div class='number'>" + adminCount + "</div><div class='label'>Admins</div></div><div class='icon'>🔑</div></div>");
        out.println("<div class='stat-card orange'><div><div class='number'>" + receptionCount + "</div><div class='label'>Receptionists</div></div><div class='icon'>🛎</div></div>");
        out.println("</div>");

        // Users table
        out.println("<div class='card'>");
        out.println("<div style='display:flex;justify-content:space-between;align-items:center;margin-bottom:16px;'>");
        out.println("<h2 style='margin-bottom:0;border:none;padding:0;'>All Staff Accounts</h2>");
        out.println("<input class='search-box' type='text' placeholder='🔍 Search staff...' oninput='filterTable(this.value)'>");
        out.println("</div>");

        if (users.isEmpty()) {
            out.println("<p style='text-align:center;color:#aaa;padding:40px;'>No users found.</p>");
        } else {
            out.println("<table id='userTable'><thead><tr>");
            out.println("<th>#</th><th>Full Name</th><th>Username</th><th>Role</th><th>Email</th><th>Status</th><th>Actions</th>");
            out.println("</tr></thead><tbody>");

            int i = 1;
            for (User u : users) {
                String roleClass   = "ADMIN".equals(u.getRole()) ? "badge-admin" : "badge-recep";
                String statusClass = u.isActive() ? "badge-active" : "badge-inactive";
                String statusText  = u.isActive() ? "Active" : "Inactive";

                out.println("<tr>");
                out.println("<td>" + i++ + "</td>");
                out.println("<td><strong>" + u.getFullName() + "</strong></td>");
                out.println("<td><code>" + u.getUsername() + "</code></td>");
                out.println("<td><span class='badge " + roleClass + "'>" + u.getRole() + "</span></td>");
                out.println("<td>" + (u.getEmail() != null ? u.getEmail() : "-") + "</td>");
                out.println("<td><span class='badge " + statusClass + "'>" + statusText + "</span></td>");
                out.println("<td>");
                out.println("<button onclick='openEditModal(" +
                            u.getUserId() + ",\"" + u.getFullName() + "\",\"" +
                            u.getUsername() + "\",\"" + u.getRole() + "\",\"" +
                            (u.getEmail() != null ? u.getEmail() : "") + "\"," +
                            u.isActive() + ")' class='btn-sm btn-edit'>✏ Edit</button>");

                // Prevent admin from deactivating themselves
                out.println("<form method='POST' action='" + contextPath + "/user' style='display:inline;margin-left:6px;'>");
                out.println("<input type='hidden' name='action'   value='update'>");
                out.println("<input type='hidden' name='userId'   value='" + u.getUserId() + "'>");
                out.println("<input type='hidden' name='fullName' value='" + u.getFullName() + "'>");
                out.println("<input type='hidden' name='role'     value='" + u.getRole() + "'>");
                out.println("<input type='hidden' name='email'    value='" + (u.getEmail() != null ? u.getEmail() : "") + "'>");
                out.println("<input type='hidden' name='isActive' value='" + !u.isActive() + "'>");
                String toggleLabel = u.isActive() ? "🔒 Deactivate" : "🔓 Activate";
                String toggleClass = u.isActive() ? "btn-sm btn-danger" : "btn-sm btn-success";
                out.println("<button type='submit' class='" + toggleClass + "'>" + toggleLabel + "</button>");
                out.println("</form>");
                out.println("</td></tr>");
            }
            out.println("</tbody></table>");
        }
        out.println("</div>"); // end card

        // Edit Modal
        out.println("<div id='editModal' class='modal' style='display:none;'>");
        out.println("<div class='modal-content'>");
        out.println("<div class='modal-header'><h3>✏ Edit Staff Account</h3><span onclick='closeModal()' style='cursor:pointer;font-size:20px;color:#888;'>✕</span></div>");
        out.println("<form method='POST' action='" + contextPath + "/user'>");
        out.println("<input type='hidden' name='action' value='update'>");
        out.println("<input type='hidden' name='userId' id='editUserId'>");
        out.println("<input type='hidden' name='isActive' value='true'>");
        out.println("<div class='form-grid'>");
        out.println("<div class='form-group'><label>Full Name *</label><input type='text' name='fullName' id='editFullName' required /></div>");
        out.println("<div class='form-group'><label>Username</label><input type='text' id='editUsername' readonly style='background:#f8f9fa;color:#888;' /></div>");
        out.println("<div class='form-group'><label>Role *</label>");
        out.println("<select name='role' id='editRole'>");
        out.println("<option value='ADMIN'>Admin</option>");
        out.println("<option value='RECEPTIONIST'>Receptionist</option>");
        out.println("</select></div>");
        out.println("<div class='form-group'><label>Email</label><input type='email' name='email' id='editEmail' /></div>");
        out.println("</div>");
        out.println("<div style='margin-top:20px;display:flex;gap:10px;'>");
        out.println("<button type='submit' class='btn btn-primary'>💾 Save Changes</button>");
        out.println("<button type='button' onclick='closeModal()' class='btn btn-secondary'>Cancel</button>");
        out.println("</div>");
        out.println("</form></div></div>");

        out.println("</div>"); // end main

        // JS
        out.println("<script>");
        out.println("function filterTable(q) {");
        out.println("  document.querySelectorAll('#userTable tbody tr').forEach(row => {");
        out.println("    row.style.display = row.textContent.toLowerCase().includes(q.toLowerCase()) ? '' : 'none';");
        out.println("  });");
        out.println("}");
        out.println("function openEditModal(id, name, username, role, email, active) {");
        out.println("  document.getElementById('editUserId').value   = id;");
        out.println("  document.getElementById('editFullName').value = name;");
        out.println("  document.getElementById('editUsername').value = username;");
        out.println("  document.getElementById('editRole').value     = role;");
        out.println("  document.getElementById('editEmail').value    = email;");
        out.println("  document.getElementById('editModal').style.display = 'flex';");
        out.println("}");
        out.println("function closeModal() {");
        out.println("  document.getElementById('editModal').style.display = 'none';");
        out.println("}");
        out.println("window.onclick = function(e) {");
        out.println("  if (e.target === document.getElementById('editModal')) closeModal();");
        out.println("}");
        out.println("</script>");

        printFooter(out);
    }

    // ------------------------------------------------
    // Show Add User Form
    // ------------------------------------------------
    private void showAddUserForm(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String contextPath = request.getContextPath();
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        printHeader(out, "Add User | Ocean View Resort", contextPath);

        out.println("<div class='main'>");
        out.println("<div class='page-header'>");
        out.println("<h1>➕ Add New Staff Account</h1>");
        out.println("<p>Create a new login account for a staff member</p>");
        out.println("</div>");

        out.println("<div class='card' style='max-width:600px;'>");
        out.println("<h2>Staff Details</h2>");
        out.println("<form method='POST' action='" + contextPath + "/user'>");
        out.println("<input type='hidden' name='action' value='add'>");
        out.println("<div class='form-grid'>");
        out.println("<div class='form-group'><label>Full Name *</label><input type='text' name='fullName' placeholder='e.g. Kasun Perera' required /></div>");
        out.println("<div class='form-group'><label>Username *</label><input type='text' name='username' placeholder='e.g. kasun123' required /></div>");
        out.println("<div class='form-group'><label>Password *</label><input type='password' name='password' placeholder='Min 6 characters' required minlength='6' /></div>");
        out.println("<div class='form-group'><label>Role *</label>");
        out.println("<select name='role'>");
        out.println("<option value='RECEPTIONIST'>Receptionist</option>");
        out.println("<option value='ADMIN'>Admin</option>");
        out.println("</select></div>");
        out.println("<div class='form-group' style='grid-column:1/-1;'><label>Email</label><input type='email' name='email' placeholder='e.g. staff@oceanviewresort.lk' /></div>");
        out.println("</div>");
        out.println("<div style='margin-top:20px;display:flex;gap:10px;'>");
        out.println("<button type='submit' class='btn btn-primary'>✅ Create Account</button>");
        out.println("<a href='" + contextPath + "/user?action=list' class='btn btn-secondary'>Cancel</a>");
        out.println("</div>");
        out.println("</form>");
        out.println("</div>");
        out.println("</div>");

        printFooter(out);
    }

    // ------------------------------------------------
    // Add new user
    // ------------------------------------------------
    private void addUser(HttpServletRequest request, HttpServletResponse response,
                         String contextPath) throws IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String fullName = request.getParameter("fullName");
        String role     = request.getParameter("role");
        String email    = request.getParameter("email");

        // Basic validation
        if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty() ||
            fullName == null || fullName.trim().isEmpty()) {
            response.sendRedirect(contextPath + "/user?action=add&error=missing");
            return;
        }

        User user = new User();
        user.setUsername(username.trim());
        user.setPassword(password.trim());
        user.setFullName(fullName.trim());
        user.setRole(role);
        user.setEmail(email != null ? email.trim() : "");
        user.setActive(true);

        boolean success = userDAO.addUser(user);
        if (success) {
            response.sendRedirect(contextPath + "/user?action=list&msg=added");
        } else {
            response.sendRedirect(contextPath + "/user?action=add&error=exists");
        }
    }

    // ------------------------------------------------
    // Update user details
    // ------------------------------------------------
    private void updateUser(HttpServletRequest request, HttpServletResponse response,
                            String contextPath) throws IOException {
        try {
            int     userId   = Integer.parseInt(request.getParameter("userId"));
            String  fullName = request.getParameter("fullName");
            String  role     = request.getParameter("role");
            String  email    = request.getParameter("email");
            boolean isActive = Boolean.parseBoolean(request.getParameter("isActive"));

            User user = new User();
            user.setUserId(userId);
            user.setFullName(fullName);
            user.setRole(role);
            user.setEmail(email != null ? email : "");
            user.setActive(isActive);

            userDAO.updateUser(user);
            response.sendRedirect(contextPath + "/user?action=list&msg=updated");

        } catch (Exception e) {
            System.err.println("[UserServlet] updateUser error: " + e.getMessage());
            response.sendRedirect(contextPath + "/user?action=list");
        }
    }

    // ------------------------------------------------
    // Shared page header
    // ------------------------------------------------
    private void printHeader(PrintWriter out, String title, String contextPath) {
        out.println("<!DOCTYPE html><html lang='en'><head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("<title>" + title + "</title>");
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
        out.println(".page-header p  { color:#888; font-size:13px; margin-top:4px; }");

        // Stats
        out.println(".stats-grid { display:grid; grid-template-columns:repeat(4,1fr); gap:20px; margin-bottom:28px; }");
        out.println(".stat-card { background:white; border-radius:14px; padding:20px 24px; box-shadow:0 2px 12px rgba(0,0,0,0.07); border-left:4px solid; display:flex; justify-content:space-between; align-items:center; }");
        out.println(".stat-card.blue   { border-color:#1b6ca8; }");
        out.println(".stat-card.green  { border-color:#16a085; }");
        out.println(".stat-card.purple { border-color:#8e44ad; }");
        out.println(".stat-card.orange { border-color:#e67e22; }");
        out.println(".stat-card .number { font-size:30px; font-weight:800; color:#1a1a2e; }");
        out.println(".stat-card .label  { font-size:12px; color:#888; margin-top:4px; }");
        out.println(".stat-card .icon   { font-size:28px; }");

        // Card
        out.println(".card { background:white; border-radius:14px; padding:24px; box-shadow:0 2px 12px rgba(0,0,0,0.07); margin-bottom:24px; }");
        out.println(".card h2 { font-size:16px; font-weight:700; color:#1a1a2e; margin-bottom:16px; padding-bottom:12px; border-bottom:2px solid #f0f4f8; }");

        // Form
        out.println(".form-grid { display:grid; grid-template-columns:1fr 1fr; gap:16px; }");
        out.println(".form-group { display:flex; flex-direction:column; gap:6px; }");
        out.println(".form-group label { font-size:13px; font-weight:600; color:#444; }");
        out.println(".form-group input, .form-group select { padding:11px 14px; border:1.5px solid #e0e0e0; border-radius:10px; font-size:14px; color:#333; outline:none; font-family:inherit; }");
        out.println(".form-group input:focus, .form-group select:focus { border-color:#1b6ca8; box-shadow:0 0 0 3px rgba(27,108,168,0.1); }");

        // Table
        out.println(".search-box { padding:9px 16px; border:1.5px solid #e0e0e0; border-radius:10px; font-size:13px; width:250px; outline:none; }");
        out.println(".search-box:focus { border-color:#1b6ca8; }");
        out.println("table { width:100%; border-collapse:collapse; }");
        out.println("th { background:#f8fafc; padding:11px 16px; text-align:left; font-size:12px; font-weight:700; color:#666; text-transform:uppercase; letter-spacing:0.5px; border-bottom:2px solid #f0f4f8; }");
        out.println("td { padding:13px 16px; font-size:13px; color:#333; border-bottom:1px solid #f8fafc; vertical-align:middle; }");
        out.println("tr:hover td { background:#fafbff; }");
        out.println("code { background:#f0f4f8; padding:3px 8px; border-radius:5px; font-size:12px; color:#1b6ca8; }");

        // Badges
        out.println(".badge { padding:4px 12px; border-radius:20px; font-size:11px; font-weight:700; }");
        out.println(".badge-admin    { background:#ede9fe; color:#5b21b6; }");
        out.println(".badge-recep    { background:#dbeafe; color:#1e40af; }");
        out.println(".badge-active   { background:#d1fae5; color:#065f46; }");
        out.println(".badge-inactive { background:#fee2e2; color:#991b1b; }");

        // Buttons
        out.println(".btn { padding:11px 22px; border:none; border-radius:10px; font-size:13px; font-weight:600; cursor:pointer; text-decoration:none; display:inline-block; transition:transform 0.2s,box-shadow 0.2s; }");
        out.println(".btn-primary   { background:linear-gradient(135deg,#0f4c75,#1b6ca8); color:white; }");
        out.println(".btn-secondary { background:#f0f4f8; color:#555; }");
        out.println(".btn:hover { transform:translateY(-2px); box-shadow:0 5px 15px rgba(0,0,0,0.12); }");
        out.println(".btn-sm { padding:6px 12px; border:none; border-radius:7px; font-size:12px; font-weight:600; cursor:pointer; }");
        out.println(".btn-edit    { background:#ede9fe; color:#5b21b6; }");
        out.println(".btn-danger  { background:#fee2e2; color:#991b1b; }");
        out.println(".btn-success { background:#d1fae5; color:#065f46; }");

        // Alert
        out.println(".alert { padding:12px 18px; border-radius:10px; font-size:13px; margin-bottom:20px; }");
        out.println(".alert-success { background:#f0fdf4; color:#16a34a; border:1px solid #bbf7d0; }");
        out.println(".alert-error   { background:#fef2f2; color:#dc2626; border:1px solid #fecaca; }");

        // Modal
        out.println(".modal { position:fixed; top:0; left:0; width:100%; height:100%; background:rgba(0,0,0,0.5); display:flex; align-items:center; justify-content:center; z-index:1000; }");
        out.println(".modal-content { background:white; border-radius:16px; padding:32px; width:520px; max-width:90%; box-shadow:0 20px 60px rgba(0,0,0,0.3); }");
        out.println(".modal-header { display:flex; justify-content:space-between; align-items:center; margin-bottom:24px; padding-bottom:14px; border-bottom:2px solid #f0f4f8; }");
        out.println(".modal-header h3 { font-size:17px; font-weight:700; color:#1a1a2e; }");

        out.println("</style></head><body>");

        // Sidebar
        out.println("<div class='sidebar'>");
        out.println("<div class='logo'><h2>🏨 Ocean View</h2></div><nav>");
        out.println("<a href='" + contextPath + "/dashboard'>🏠 Dashboard</a>");
        out.println("<a href='" + contextPath + "/reservation?action=add'>➕ New Reservation</a>");
        out.println("<a href='" + contextPath + "/reservation?action=list'>📋 All Reservations</a>");
        out.println("<a href='" + contextPath + "/room?action=list'>🛏 Rooms</a>");
        out.println("<a href='" + contextPath + "/service?action=list'>⭐ Services</a>");
        out.println("<a href='" + contextPath + "/bill'>💰 Billing</a>");
        out.println("<a href='" + contextPath + "/profile'>👤 My Profile</a>");
        out.println("<a href='" + contextPath + "/help' class='active'>❓ Help & Guide</a>");
        out.println("<a href='" + contextPath + "/user?action=list' class='active'>👥 Manage Users</a>");
        out.println("</nav>");
        out.println("<div class='logout'><a href='" + contextPath + "/logout'>🚪 Logout</a></div>");
        out.println("</div>");
    }

    // ------------------------------------------------
    // Shared page footer
    // ------------------------------------------------
    private void printFooter(PrintWriter out) {
        out.println("</body></html>");
    }
}