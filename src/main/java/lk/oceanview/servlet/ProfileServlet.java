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

/**
 * ProfileServlet
 * GET  -> show profile and password change form
 * POST -> process password change
 */
public class ProfileServlet extends HttpServlet {

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
        String msg         = request.getParameter("msg");
        String error       = request.getParameter("error");

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        printPage(out, contextPath, user, msg, error);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedInUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login.html?error=session");
            return;
        }

        User   user        = (User) session.getAttribute("loggedInUser");
        String contextPath = request.getContextPath();

        String currentPassword = request.getParameter("currentPassword");
        String newPassword     = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");

        // Validation
        if (currentPassword == null || currentPassword.trim().isEmpty() ||
            newPassword     == null || newPassword.trim().isEmpty()     ||
            confirmPassword == null || confirmPassword.trim().isEmpty()) {
            response.sendRedirect(contextPath + "/profile?error=missing");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            response.sendRedirect(contextPath + "/profile?error=mismatch");
            return;
        }

        if (newPassword.length() < 6) {
            response.sendRedirect(contextPath + "/profile?error=tooshort");
            return;
        }

        if (newPassword.equals(currentPassword)) {
            response.sendRedirect(contextPath + "/profile?error=samepassword");
            return;
        }

        // Verify current password is correct
        if (!verifyCurrentPassword(user.getUserId(), currentPassword)) {
            response.sendRedirect(contextPath + "/profile?error=wrongpassword");
            return;
        }

        // Update password
        boolean success = updatePassword(user.getUserId(), newPassword);
        if (success) {
            response.sendRedirect(contextPath + "/profile?msg=success");
        } else {
            response.sendRedirect(contextPath + "/profile?error=savefailed");
        }
    }

    // ------------------------------------------------
    // Verify current password against DB
    // ------------------------------------------------
    private boolean verifyCurrentPassword(int userId, String password) {
        String sql = "SELECT user_id FROM users WHERE user_id = ? AND password = SHA2(?, 256)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("[ProfileServlet] verifyCurrentPassword: " + e.getMessage());
        }
        return false;
    }

    // ------------------------------------------------
    // Update password in DB
    // ------------------------------------------------
    private boolean updatePassword(int userId, String newPassword) {
        String sql = "UPDATE users SET password = SHA2(?, 256) WHERE user_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ProfileServlet] updatePassword: " + e.getMessage());
        }
        return false;
    }

    // ------------------------------------------------
    // Render page
    // ------------------------------------------------
    private void printPage(PrintWriter out, String contextPath, User user,
                           String msg, String error) {

        // Resolve alert message
        String alertHtml = "";
        if ("success".equals(msg)) {
            alertHtml = "<div class='alert alert-success'>✅ Password changed successfully!</div>";
        } else if (error != null) {
            String errText = error.equals("missing")      ? "All fields are required."                          :
                             error.equals("mismatch")     ? "New password and confirm password do not match."   :
                             error.equals("tooshort")     ? "New password must be at least 6 characters long."  :
                             error.equals("samepassword") ? "New password must be different from your current password." :
                             error.equals("wrongpassword")? "Current password is incorrect."                    :
                             "Something went wrong. Please try again.";
            alertHtml = "<div class='alert alert-error'>⚠ " + errText + "</div>";
        }

        out.println("<!DOCTYPE html><html lang='en'><head>");
        out.println("<meta charset='UTF-8'><meta name='viewport' content='width=device-width,initial-scale=1.0'>");
        out.println("<title>My Profile | Ocean View Resort</title>");
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
        out.println(".main { margin-left:240px; padding:30px; max-width:900px; }");
        out.println(".page-header { margin-bottom:24px; }");
        out.println(".page-header h1 { font-size:22px; color:#1a1a2e; font-weight:700; }");
        out.println(".page-header p  { color:#888; font-size:13px; margin-top:4px; }");

        // Profile card
        out.println(".profile-card { background:white; border-radius:14px; padding:28px; box-shadow:0 2px 12px rgba(0,0,0,0.07); margin-bottom:24px; display:flex; gap:24px; align-items:center; }");
        out.println(".avatar { width:72px; height:72px; border-radius:50%; background:linear-gradient(135deg,#0f4c75,#1b6ca8); color:white; font-size:28px; font-weight:800; display:flex; align-items:center; justify-content:center; flex-shrink:0; }");
        out.println(".profile-info h2 { font-size:20px; font-weight:800; color:#1a1a2e; margin-bottom:6px; }");
        out.println(".profile-info p  { font-size:13px; color:#666; margin-bottom:4px; }");
        out.println(".role-badge { padding:4px 14px; border-radius:20px; font-size:12px; font-weight:700; display:inline-block; margin-top:4px; }");
        out.println(".badge-admin { background:#ede9fe; color:#5b21b6; }");
        out.println(".badge-recep { background:#dbeafe; color:#1e40af; }");

        // Password form card
        out.println(".card { background:white; border-radius:14px; padding:28px; box-shadow:0 2px 12px rgba(0,0,0,0.07); margin-bottom:24px; max-width:500px; }");
        out.println(".card h2 { font-size:16px; font-weight:700; color:#1a1a2e; margin-bottom:20px; padding-bottom:12px; border-bottom:2px solid #f0f4f8; }");
        out.println(".form-group { display:flex; flex-direction:column; gap:6px; margin-bottom:16px; }");
        out.println(".form-group label { font-size:13px; font-weight:600; color:#444; }");
        out.println(".form-group input { padding:11px 14px; border:1.5px solid #e0e0e0; border-radius:10px; font-size:14px; outline:none; font-family:inherit; }");
        out.println(".form-group input:focus { border-color:#1b6ca8; box-shadow:0 0 0 3px rgba(27,108,168,0.1); }");
        out.println(".password-hint { font-size:11px; color:#aaa; margin-top:4px; }");

        // Strength bar
        out.println(".strength-bar { height:4px; border-radius:4px; background:#f0f0f0; margin-top:6px; overflow:hidden; }");
        out.println(".strength-fill { height:100%; border-radius:4px; width:0; transition:width 0.3s,background 0.3s; }");
        out.println(".strength-label { font-size:11px; margin-top:4px; font-weight:600; }");

        // Buttons
        out.println(".btn { padding:11px 24px; border:none; border-radius:10px; font-size:14px; font-weight:600; cursor:pointer; text-decoration:none; display:inline-block; transition:transform 0.2s,box-shadow 0.2s; }");
        out.println(".btn-primary   { background:linear-gradient(135deg,#0f4c75,#1b6ca8); color:white; }");
        out.println(".btn-secondary { background:#f0f4f8; color:#555; margin-left:10px; }");
        out.println(".btn:hover { transform:translateY(-2px); box-shadow:0 5px 15px rgba(0,0,0,0.12); }");

        // Alerts
        out.println(".alert { padding:12px 18px; border-radius:10px; font-size:13px; margin-bottom:20px; max-width:500px; }");
        out.println(".alert-success { background:#f0fdf4; color:#16a34a; border:1px solid #bbf7d0; }");
        out.println(".alert-error   { background:#fef2f2; color:#dc2626; border:1px solid #fecaca; }");

        out.println("</style></head><body>");

        // Sidebar
        out.println("<div class='sidebar'><div class='logo'><h2>🏨 Ocean View</h2></div><nav>");
        out.println("<a href='" + contextPath + "/dashboard'>🏠 Dashboard</a>");
        out.println("<a href='" + contextPath + "/reservation?action=add'>➕ New Reservation</a>");
        out.println("<a href='" + contextPath + "/reservation?action=list'>📋 All Reservations</a>");
        out.println("<a href='" + contextPath + "/guest?action=history'>👤 Guest History</a>");
        out.println("<a href='" + contextPath + "/room?action=list'>🛏 Rooms</a>");
        out.println("<a href='" + contextPath + "/service?action=list'>⭐ Services</a>");
        out.println("<a href='" + contextPath + "/bill'>💰 Billing</a>");
        out.println("<a href='" + contextPath + "/report'>📊 Reports</a>");
        if (user.isAdmin()) out.println("<a href='" + contextPath + "/user?action=list'>👥 Manage Users</a>");
        out.println("<a href='" + contextPath + "/profile' class='active'>👤 My Profile</a>");
        out.println("<a href='" + contextPath + "/help'>❓ Help & Guide</a>");
        out.println("</nav><div class='logout'><a href='" + contextPath + "/logout'>🚪 Logout</a></div></div>");

        // Main
        out.println("<div class='main'>");
        out.println("<div class='page-header'><h1>👤 My Profile</h1><p>View your account details and change your password</p></div>");

        // Alert
        out.println(alertHtml);

        // Profile summary card
        String initials   = user.getFullName().substring(0, 1).toUpperCase();
        String badgeClass = "ADMIN".equals(user.getRole()) ? "badge-admin" : "badge-recep";
        out.println("<div class='profile-card'>");
        out.println("<div class='avatar'>" + initials + "</div>");
        out.println("<div class='profile-info'>");
        out.println("<h2>" + user.getFullName() + "</h2>");
        out.println("<p>👤 Username: <strong>" + user.getUsername() + "</strong></p>");
        out.println("<p>📧 Email: <strong>" + (user.getEmail() != null && !user.getEmail().isEmpty() ? user.getEmail() : "Not set") + "</strong></p>");
        out.println("<span class='role-badge " + badgeClass + "'>" + user.getRole() + "</span>");
        out.println("</div></div>");

        // Password change form
        out.println("<div class='card'>");
        out.println("<h2>🔒 Change Password</h2>");
        out.println("<form method='POST' action='" + contextPath + "/profile'>");

        out.println("<div class='form-group'>");
        out.println("<label>Current Password *</label>");
        out.println("<input type='password' name='currentPassword' placeholder='Enter your current password' required />");
        out.println("</div>");

        out.println("<div class='form-group'>");
        out.println("<label>New Password *</label>");
        out.println("<input type='password' name='newPassword' id='newPassword' placeholder='At least 6 characters' required oninput='checkStrength(this.value)' />");
        out.println("<div class='strength-bar'><div class='strength-fill' id='strengthFill'></div></div>");
        out.println("<div class='strength-label' id='strengthLabel'></div>");
        out.println("</div>");

        out.println("<div class='form-group'>");
        out.println("<label>Confirm New Password *</label>");
        out.println("<input type='password' name='confirmPassword' id='confirmPassword' placeholder='Re-enter new password' required oninput='checkMatch()' />");
        out.println("<div class='password-hint' id='matchHint'></div>");
        out.println("</div>");

        out.println("<div style='margin-top:8px;'>");
        out.println("<button type='submit' class='btn btn-primary'>🔒 Change Password</button>");
        out.println("<a href='" + contextPath + "/dashboard' class='btn btn-secondary'>Cancel</a>");
        out.println("</div></form></div>");

        out.println("</div>"); // end main

        // JS - password strength + match check
        out.println("<script>");
        out.println("function checkStrength(val) {");
        out.println("  const fill  = document.getElementById('strengthFill');");
        out.println("  const label = document.getElementById('strengthLabel');");
        out.println("  let strength = 0;");
        out.println("  if (val.length >= 6)  strength++;");
        out.println("  if (val.length >= 10) strength++;");
        out.println("  if (/[A-Z]/.test(val)) strength++;");
        out.println("  if (/[0-9]/.test(val)) strength++;");
        out.println("  if (/[^A-Za-z0-9]/.test(val)) strength++;");
        out.println("  const colors = ['#dc2626','#f97316','#eab308','#16a34a','#16a34a'];");
        out.println("  const labels = ['Very Weak','Weak','Fair','Strong','Very Strong'];");
        out.println("  const widths = ['20%','40%','60%','80%','100%'];");
        out.println("  if (val.length === 0) { fill.style.width='0'; label.textContent=''; return; }");
        out.println("  const i = Math.min(strength - 1, 4);");
        out.println("  fill.style.width    = widths[i];");
        out.println("  fill.style.background = colors[i];");
        out.println("  label.textContent   = labels[i];");
        out.println("  label.style.color   = colors[i];");
        out.println("}");
        out.println("function checkMatch() {");
        out.println("  const np   = document.getElementById('newPassword').value;");
        out.println("  const cp   = document.getElementById('confirmPassword').value;");
        out.println("  const hint = document.getElementById('matchHint');");
        out.println("  if (cp.length === 0) { hint.textContent = ''; return; }");
        out.println("  if (np === cp) { hint.textContent = '✅ Passwords match'; hint.style.color = '#16a34a'; }");
        out.println("  else          { hint.textContent = '❌ Passwords do not match'; hint.style.color = '#dc2626'; }");
        out.println("}");
        out.println("</script>");

        out.println("</body></html>");
    }
}