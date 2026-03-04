package lk.oceanview.servlet;

import lk.oceanview.model.User;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * HelpServlet
 * GET -> show help and user guide page
 */
public class HelpServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Session check
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedInUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login.html?error=session");
            return;
        }

        User user          = (User) session.getAttribute("loggedInUser");
        String contextPath = request.getContextPath();
        String section     = request.getParameter("section") != null
                             ? request.getParameter("section") : "getting-started";

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html><html lang='en'><head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("<title>Help & Guide | Ocean View Resort</title>");
        out.println("<style>");
        out.println("* { margin:0; padding:0; box-sizing:border-box; }");
        out.println("body { font-family:'Segoe UI',sans-serif; background:#f0f4f8; }");

        // Sidebar
        out.println(".sidebar { width:240px; height:100vh; background:linear-gradient(180deg,#0f4c75,#1b6ca8); position:fixed; top:0; left:0; padding:30px 0; overflow-y:auto; }");
        out.println(".sidebar .logo { text-align:center; padding:0 20px 30px; border-bottom:1px solid rgba(255,255,255,0.15); }");
        out.println(".sidebar .logo h2 { font-size:16px; font-weight:700; color:white; }");
        out.println(".sidebar nav a { display:block; padding:12px 25px; color:rgba(255,255,255,0.85); text-decoration:none; font-size:14px; }");
        out.println(".sidebar nav a:hover, .sidebar nav a.active { background:rgba(255,255,255,0.15); border-left:3px solid white; color:white; }");
        out.println(".sidebar .logout { position:absolute; bottom:20px; width:100%; padding:0 20px; }");
        out.println(".sidebar .logout a { display:block; padding:11px; text-align:center; background:rgba(255,255,255,0.15); color:white; border-radius:8px; text-decoration:none; font-size:13px; }");
        out.println(".sidebar .logout a:hover { background:rgba(255,80,80,0.4); }");

        // Main layout - two column
        out.println(".main { margin-left:240px; padding:30px; display:grid; grid-template-columns:220px 1fr; gap:24px; }");

        // Help nav (left column)
        out.println(".help-nav { position:sticky; top:30px; height:fit-content; }");
        out.println(".help-nav .nav-card { background:white; border-radius:14px; padding:16px; box-shadow:0 2px 12px rgba(0,0,0,0.07); }");
        out.println(".help-nav .nav-card h3 { font-size:12px; font-weight:700; color:#888; text-transform:uppercase; letter-spacing:0.5px; margin-bottom:12px; padding-bottom:8px; border-bottom:2px solid #f0f4f8; }");
        out.println(".help-nav a { display:flex; align-items:center; gap:8px; padding:9px 12px; border-radius:8px; text-decoration:none; font-size:13px; color:#555; font-weight:500; margin-bottom:2px; transition:all 0.2s; }");
        out.println(".help-nav a:hover { background:#f0f7ff; color:#1b6ca8; }");
        out.println(".help-nav a.active { background:#dbeafe; color:#1e40af; font-weight:700; }");

        // Content (right column)
        out.println(".help-content { min-width:0; }");
        out.println(".content-card { background:white; border-radius:14px; padding:32px; box-shadow:0 2px 12px rgba(0,0,0,0.07); margin-bottom:24px; }");
        out.println(".content-card h1 { font-size:22px; font-weight:800; color:#1a1a2e; margin-bottom:6px; }");
        out.println(".content-card .subtitle { font-size:14px; color:#888; margin-bottom:28px; padding-bottom:20px; border-bottom:2px solid #f0f4f8; }");
        out.println(".content-card h2 { font-size:16px; font-weight:700; color:#0f4c75; margin:28px 0 12px; display:flex; align-items:center; gap:8px; }");
        out.println(".content-card h2:first-of-type { margin-top:0; }");
        out.println(".content-card p { font-size:14px; color:#444; line-height:1.7; margin-bottom:12px; }");
        out.println(".content-card ul { padding-left:20px; margin-bottom:12px; }");
        out.println(".content-card ul li { font-size:14px; color:#444; line-height:1.8; }");

        // Steps
        out.println(".steps { display:flex; flex-direction:column; gap:14px; margin:16px 0; }");
        out.println(".step { display:flex; gap:16px; align-items:flex-start; background:#f8faff; border-radius:12px; padding:16px; border-left:4px solid #1b6ca8; }");
        out.println(".step .step-num { background:linear-gradient(135deg,#0f4c75,#1b6ca8); color:white; width:28px; height:28px; border-radius:50%; display:flex; align-items:center; justify-content:center; font-size:13px; font-weight:700; flex-shrink:0; }");
        out.println(".step .step-body h4 { font-size:14px; font-weight:700; color:#1a1a2e; margin-bottom:4px; }");
        out.println(".step .step-body p  { font-size:13px; color:#666; margin:0; line-height:1.6; }");

        // Info boxes
        out.println(".info-box { border-radius:10px; padding:14px 18px; margin:12px 0; font-size:13px; line-height:1.6; }");
        out.println(".info-box.blue   { background:#eff6ff; border-left:4px solid #3b82f6; color:#1e40af; }");
        out.println(".info-box.green  { background:#f0fdf4; border-left:4px solid #16a34a; color:#065f46; }");
        out.println(".info-box.orange { background:#fff7ed; border-left:4px solid #f97316; color:#9a3412; }");
        out.println(".info-box.red    { background:#fef2f2; border-left:4px solid #dc2626; color:#991b1b; }");

        // Role table
        out.println(".role-table { width:100%; border-collapse:collapse; margin:12px 0; font-size:13px; }");
        out.println(".role-table th { background:#f0f4f8; padding:10px 14px; text-align:left; font-weight:700; color:#555; border-bottom:2px solid #e0e0e0; }");
        out.println(".role-table td { padding:10px 14px; border-bottom:1px solid #f0f4f8; color:#444; vertical-align:top; }");
        out.println(".role-table tr:hover td { background:#fafbff; }");
        out.println(".tick { color:#16a34a; font-weight:700; } .cross { color:#dc2626; font-weight:700; }");

        // Badge
        out.println(".badge { padding:3px 10px; border-radius:20px; font-size:11px; font-weight:700; display:inline-block; }");
        out.println(".badge-admin { background:#ede9fe; color:#5b21b6; }");
        out.println(".badge-recep { background:#dbeafe; color:#1e40af; }");

        out.println("</style></head><body>");

        // ---- App Sidebar ----
        out.println("<div class='sidebar'><div class='logo'><h2>🏨 Ocean View</h2></div><nav>");
        out.println("<a href='" + contextPath + "/dashboard'>🏠 Dashboard</a>");
        out.println("<a href='" + contextPath + "/reservation?action=add'>➕ New Reservation</a>");
        out.println("<a href='" + contextPath + "/reservation?action=list'>📋 All Reservations</a>");
        out.println("<a href='" + contextPath + "/guest?action=history'>👤 Guest History</a>");
        out.println("<a href='" + contextPath + "/room?action=list'>🛏 Rooms</a>");
        out.println("<a href='" + contextPath + "/service?action=list'>⭐ Services</a>");
        out.println("<a href='" + contextPath + "/bill'>💰 Billing</a>");
        out.println("<a href='" + contextPath + "/report'>ð Reports</a>");
        if (user.isAdmin()) out.println("<a href='" + contextPath + "/user?action=list'>👥 Manage Users</a>");
        out.println("<a href='" + contextPath + "/profile'>👤 My Profile</a>");
        out.println("<a href='" + contextPath + "/help' class='active'>❓ Help & Guide</a>");
        out.println("</nav><div class='logout'><a href='" + contextPath + "/logout'>🚪 Logout</a></div></div>");

        // ---- Main two-column layout ----
        out.println("<div class='main'>");

        // ---- Help Nav (left) ----
        out.println("<div class='help-nav'><div class='nav-card'>");
        out.println("<h3>📖 Contents</h3>");
        printNavLink(out, contextPath, section, "getting-started", "🚀", "Getting Started");
        printNavLink(out, contextPath, section, "reservations",    "📋", "Reservations");
        printNavLink(out, contextPath, section, "checkin",         "🏨", "Check-in & Out");
        printNavLink(out, contextPath, section, "billing",         "💰", "Billing");
        printNavLink(out, contextPath, section, "rooms",           "🛏", "Rooms");
        printNavLink(out, contextPath, section, "services",        "⭐", "Services");
        if (user.isAdmin()) printNavLink(out, contextPath, section, "users", "👥", "User Management");
        printNavLink(out, contextPath, section, "roles",           "🔑", "Roles & Permissions");
        printNavLink(out, contextPath, section, "troubleshooting", "🔧", "Troubleshooting");
        out.println("</div></div>");

        // ---- Help Content (right) ----
        out.println("<div class='help-content'>");

        switch (section) {

            // ================================================
            case "getting-started":
            // ================================================
                out.println("<div class='content-card'>");
                out.println("<h1>🚀 Getting Started</h1>");
                out.println("<p class='subtitle'>Welcome to the Ocean View Resort Reservation System. This guide will help you get up and running quickly.</p>");

                out.println("<h2>🏨 About This System</h2>");
                out.println("<p>The Ocean View Resort Reservation System is a web-based application that allows staff to manage guest reservations, room allocations, billing, and resort services from a single interface.</p>");

                out.println("<h2>🔐 Logging In</h2>");
                out.println("<div class='steps'>");
                out.println("<div class='step'><div class='step-num'>1</div><div class='step-body'><h4>Open the login page</h4><p>Navigate to the system URL provided by your manager and enter your username and password.</p></div></div>");
                out.println("<div class='step'><div class='step-num'>2</div><div class='step-body'><h4>Enter your credentials</h4><p>Use the username and password assigned to you. Contact your administrator if you do not have an account.</p></div></div>");
                out.println("<div class='step'><div class='step-num'>3</div><div class='step-body'><h4>You're in!</h4><p>You will be redirected to the Dashboard which shows today's key stats at a glance.</p></div></div>");
                out.println("</div>");

                out.println("<div class='info-box blue'>💡 <strong>Tip:</strong> Your session will expire after 30 minutes of inactivity. You will be redirected to the login page automatically.</div>");

                out.println("<h2>🗺 Navigation</h2>");
                out.println("<p>Use the sidebar on the left to navigate between sections. The sidebar is always visible so you can jump to any section at any time.</p>");
                out.println("<ul>");
                out.println("<li><strong>🏠 Dashboard</strong> — Overview of today's activity</li>");
                out.println("<li><strong>➕ New Reservation</strong> — Book a room for a guest</li>");
                out.println("<li><strong>📋 All Reservations</strong> — View and manage all bookings</li>");
                out.println("<li><strong>🛏 Rooms</strong> — View room availability and status</li>");
                out.println("<li><strong>⭐ Services</strong> — View resort services offered to guests</li>");
                out.println("<li><strong>💰 Billing</strong> — Generate and manage guest bills</li>");
                out.println("<li><strong>👥 Manage Users</strong> — Admin only: manage staff accounts</li>");
                out.println("</ul>");
                out.println("</div>");
                break;

            // ================================================
            case "reservations":
            // ================================================
                out.println("<div class='content-card'>");
                out.println("<h1>📋 Reservations</h1>");
                out.println("<p class='subtitle'>How to create, view, and manage guest reservations.</p>");

                out.println("<h2>➕ Creating a New Reservation</h2>");
                out.println("<div class='steps'>");
                out.println("<div class='step'><div class='step-num'>1</div><div class='step-body'><h4>Go to New Reservation</h4><p>Click \"➕ New Reservation\" in the sidebar.</p></div></div>");
                out.println("<div class='step'><div class='step-num'>2</div><div class='step-body'><h4>Enter guest NIC / Passport</h4><p>Type the guest's NIC number. If they are a returning guest, their details will auto-fill. For new guests, fill in the form manually.</p></div></div>");
                out.println("<div class='step'><div class='step-num'>3</div><div class='step-body'><h4>Select a room</h4><p>Choose from the available rooms dropdown. The room type and rate will be shown automatically.</p></div></div>");
                out.println("<div class='step'><div class='step-num'>4</div><div class='step-body'><h4>Set dates</h4><p>Select check-in and check-out dates. The number of nights and total cost will be calculated automatically.</p></div></div>");
                out.println("<div class='step'><div class='step-num'>5</div><div class='step-body'><h4>Confirm</h4><p>Click \"✅ Confirm Reservation\". The reservation will be saved with status CONFIRMED.</p></div></div>");
                out.println("</div>");

                out.println("<div class='info-box green'>✅ <strong>Returning Guests:</strong> When you type a NIC number that already exists in the system, the guest's name, address, contact and email will auto-fill — saving you time!</div>");

                out.println("<h2>🔍 Searching Reservations</h2>");
                out.println("<p>On the All Reservations page, use the search bar at the top right to filter reservations by guest name or reservation number in real time.</p>");

                out.println("<h2>👁 Viewing a Reservation</h2>");
                out.println("<p>Click <strong>View</strong> on any reservation in the list to see its full details including guest info, room, dates, and total cost.</p>");

                out.println("<div class='info-box orange'>⚠ <strong>Note:</strong> Once a reservation is CANCELLED or CHECKED_OUT, it cannot be modified. Always double-check details before confirming.</div>");
                out.println("</div>");
                break;

            // ================================================
            case "checkin":
            // ================================================
                out.println("<div class='content-card'>");
                out.println("<h1>🏨 Check-in & Check-out</h1>");
                out.println("<p class='subtitle'>How to manage guest arrivals and departures.</p>");

                out.println("<h2>🏨 Checking a Guest In</h2>");
                out.println("<div class='steps'>");
                out.println("<div class='step'><div class='step-num'>1</div><div class='step-body'><h4>Find the reservation</h4><p>Go to All Reservations and search for the guest's name or reservation number.</p></div></div>");
                out.println("<div class='step'><div class='step-num'>2</div><div class='step-body'><h4>Open the reservation</h4><p>Click View to open the reservation details page.</p></div></div>");
                out.println("<div class='step'><div class='step-num'>3</div><div class='step-body'><h4>Click Check In</h4><p>Click the green \"🏨 Check In\" button. The reservation status will change to CHECKED_IN and the room status will be set to OCCUPIED automatically.</p></div></div>");
                out.println("</div>");

                out.println("<h2>🚪 Checking a Guest Out</h2>");
                out.println("<div class='steps'>");
                out.println("<div class='step'><div class='step-num'>1</div><div class='step-body'><h4>Find the reservation</h4><p>Search for the guest's active reservation (status: CHECKED_IN).</p></div></div>");
                out.println("<div class='step'><div class='step-num'>2</div><div class='step-body'><h4>Click Check Out</h4><p>Click the orange \"🚪 Check Out\" button. The room will automatically be set back to AVAILABLE.</p></div></div>");
                out.println("<div class='step'><div class='step-num'>3</div><div class='step-body'><h4>Generate the bill</h4><p>Click \"🧾 Generate Bill\" to create the final invoice for the guest before they leave.</p></div></div>");
                out.println("</div>");

                out.println("<h2>❌ Cancelling a Reservation</h2>");
                out.println("<p>On the reservation details page, click <strong>\"❌ Cancel Reservation\"</strong>. A confirmation prompt will appear. Once confirmed, the reservation is cancelled and the room is freed.</p>");
                out.println("<div class='info-box red'>⚠ <strong>Warning:</strong> Cancellations are permanent and cannot be undone. Make sure the guest has confirmed the cancellation before proceeding.</div>");
                out.println("</div>");
                break;

            // ================================================
            case "billing":
            // ================================================
                out.println("<div class='content-card'>");
                out.println("<h1>💰 Billing</h1>");
                out.println("<p class='subtitle'>How to generate and manage guest invoices.</p>");

                out.println("<h2>🧾 Generating a Bill</h2>");
                out.println("<div class='steps'>");
                out.println("<div class='step'><div class='step-num'>1</div><div class='step-body'><h4>Go to Billing</h4><p>Click \"💰 Billing\" in the sidebar, or click \"🧾 Generate Bill\" directly from a reservation's detail page.</p></div></div>");
                out.println("<div class='step'><div class='step-num'>2</div><div class='step-body'><h4>Find the reservation</h4><p>Enter the reservation number (e.g. OVR-2025-0001) or click directly from the list shown on the billing page.</p></div></div>");
                out.println("<div class='step'><div class='step-num'>3</div><div class='step-body'><h4>Review the invoice</h4><p>The bill preview shows room charges, 10% VAT, and the total. You can also apply a discount if applicable.</p></div></div>");
                out.println("<div class='step'><div class='step-num'>4</div><div class='step-body'><h4>Select payment method</h4><p>Choose Cash, Card, or Online and click \"✅ Confirm & Save Bill\".</p></div></div>");
                out.println("<div class='step'><div class='step-num'>5</div><div class='step-body'><h4>Print the bill</h4><p>Click \"🖨 Print Bill\" to print a clean receipt for the guest. The sidebar will automatically hide when printing.</p></div></div>");
                out.println("</div>");

                out.println("<h2>💵 Bill Breakdown</h2>");
                out.println("<ul>");
                out.println("<li><strong>Room Charges</strong> = Rate per night × Number of nights</li>");
                out.println("<li><strong>VAT (10%)</strong> = Applied to room charges</li>");
                out.println("<li><strong>Discount</strong> = Optional amount deducted (entered manually)</li>");
                out.println("<li><strong>Total</strong> = Room Charges + VAT − Discount</li>");
                out.println("</ul>");

                out.println("<div class='info-box blue'>💡 Once a bill is generated, click <strong>\"💰 Mark as Paid\"</strong> after the guest has settled the payment to update the payment status.</div>");
                out.println("</div>");
                break;

            // ================================================
            case "rooms":
            // ================================================
                out.println("<div class='content-card'>");
                out.println("<h1>🛏 Rooms</h1>");
                out.println("<p class='subtitle'>How to view and manage rooms and room types.</p>");

                out.println("<h2>📊 Room Status Colours</h2>");
                out.println("<ul>");
                out.println("<li><strong style='color:#16a085;'>Green</strong> — Available (room is ready to be booked)</li>");
                out.println("<li><strong style='color:#e74c3c;'>Red</strong> — Occupied (guest is currently staying)</li>");
                out.println("<li><strong style='color:#e67e22;'>Orange</strong> — Maintenance (room is out of service)</li>");
                out.println("</ul>");

                out.println("<h2>🔄 Changing Room Status</h2>");
                out.println("<p>Any staff member can change a room's status using the dropdown on each room card. This is useful for marking rooms as under maintenance.</p>");
                out.println("<div class='info-box orange'>⚠ Room status is also updated <strong>automatically</strong> when a guest checks in or checks out — you don't need to change it manually for normal reservations.</div>");

                out.println("<h2>➕ Adding a Room <span class='badge badge-admin'>Admin</span></h2>");
                out.println("<p>Click \"➕ Add Room\", select the room type, enter the room number and floor, then click Save.</p>");

                out.println("<h2>📂 Room Types <span class='badge badge-admin'>Admin</span></h2>");
                out.println("<p>Click the <strong>Room Types</strong> tab to manage room categories. Each room type has a name, description, rate per night, and maximum occupancy. Rooms inherit their rate from their assigned room type.</p>");
                out.println("<div class='info-box red'>⚠ You cannot delete a room type that still has rooms assigned to it. Reassign or delete those rooms first.</div>");
                out.println("</div>");
                break;

            // ================================================
            case "services":
            // ================================================
                out.println("<div class='content-card'>");
                out.println("<h1>⭐ Services</h1>");
                out.println("<p class='subtitle'>How to manage resort services offered to guests.</p>");

                out.println("<h2>📋 Viewing Services</h2>");
                out.println("<p>All staff can view the full list of resort services grouped by category: Dining, Spa & Wellness, Recreation, Transport, Room Service, and Other.</p>");

                out.println("<h2>➕ Adding a Service <span class='badge badge-admin'>Admin</span></h2>");
                out.println("<p>Click \"➕ Add New Service\", fill in the service name, category, price, and an optional description, then click Save.</p>");

                out.println("<h2>🔒 Disabling a Service <span class='badge badge-admin'>Admin</span></h2>");
                out.println("<p>If a service is temporarily unavailable (e.g. the spa is closed for renovation), click <strong>\"🔒 Disable\"</strong> on the service card. It will show as Unavailable but remain in the system. Re-enable it when it becomes available again.</p>");

                out.println("<div class='info-box green'>✅ Disabled services are shown in the list so staff are aware they exist, but guests will not be offered them during booking.</div>");
                out.println("</div>");
                break;

            // ================================================
            case "users":
            // ================================================
                out.println("<div class='content-card'>");
                out.println("<h1>👥 User Management <span class='badge badge-admin'>Admin Only</span></h1>");
                out.println("<p class='subtitle'>How to manage staff accounts.</p>");

                out.println("<h2>➕ Adding a New Staff Account</h2>");
                out.println("<div class='steps'>");
                out.println("<div class='step'><div class='step-num'>1</div><div class='step-body'><h4>Go to Manage Users</h4><p>Click \"👥 Manage Users\" in the sidebar (visible to Admins only).</p></div></div>");
                out.println("<div class='step'><div class='step-num'>2</div><div class='step-body'><h4>Click Add New User</h4><p>Fill in the staff member's full name, username, password, role, and email.</p></div></div>");
                out.println("<div class='step'><div class='step-num'>3</div><div class='step-body'><h4>Choose the role</h4><p>Select <strong>Admin</strong> for managers who need full access, or <strong>Receptionist</strong> for front desk staff.</p></div></div>");
                out.println("</div>");

                out.println("<h2>✏ Editing a Staff Account</h2>");
                out.println("<p>Click the <strong>✏ Edit</strong> button next to any staff member to update their name, role, or email. Note: Usernames cannot be changed after creation.</p>");

                out.println("<h2>🔒 Deactivating an Account</h2>");
                out.println("<p>Click <strong>\"🔒 Deactivate\"</strong> to disable a staff account without deleting it. The staff member will no longer be able to log in. You can reactivate the account at any time by clicking <strong>\"🔓 Activate\"</strong>.</p>");
                out.println("<div class='info-box orange'>⚠ Deactivating an account does not delete any data associated with that user.</div>");
                out.println("</div>");
                break;

            // ================================================
            case "roles":
            // ================================================
                out.println("<div class='content-card'>");
                out.println("<h1>🔑 Roles & Permissions</h1>");
                out.println("<p class='subtitle'>What each role can and cannot do in the system.</p>");

                out.println("<table class='role-table'>");
                out.println("<thead><tr><th>Feature</th><th><span class='badge badge-admin'>Admin</span></th><th><span class='badge badge-recep'>Receptionist</span></th></tr></thead>");
                out.println("<tbody>");
                printRoleRow(out, "View Dashboard",             true, true);
                printRoleRow(out, "Create Reservation",         true, true);
                printRoleRow(out, "View All Reservations",      true, true);
                printRoleRow(out, "Check-in / Check-out",       true, true);
                printRoleRow(out, "Cancel Reservation",         true, true);
                printRoleRow(out, "Generate Bill",              true, true);
                printRoleRow(out, "Mark Bill as Paid",          true, true);
                printRoleRow(out, "Print Bill",                 true, true);
                printRoleRow(out, "View Rooms",                 true, true);
                printRoleRow(out, "Change Room Status",         true, true);
                printRoleRow(out, "Add / Edit / Delete Rooms",  true, false);
                printRoleRow(out, "Manage Room Types",          true, false);
                printRoleRow(out, "View Services",              true, true);
                printRoleRow(out, "Add / Edit / Delete Services", true, false);
                printRoleRow(out, "Manage Staff Accounts",      true, false);
                out.println("</tbody></table>");

                out.println("<div class='info-box blue' style='margin-top:20px;'>💡 If you need access to a feature that is restricted, contact your system administrator.</div>");
                out.println("</div>");
                break;

            // ================================================
            case "troubleshooting":
            // ================================================
                out.println("<div class='content-card'>");
                out.println("<h1>🔧 Troubleshooting</h1>");
                out.println("<p class='subtitle'>Common issues and how to resolve them.</p>");

                out.println("<h2>🔐 I can't log in</h2>");
                out.println("<ul>");
                out.println("<li>Make sure your username and password are correct (passwords are case-sensitive).</li>");
                out.println("<li>Your account may have been deactivated. Contact your administrator.</li>");
                out.println("<li>Your session may have expired — try refreshing the page.</li>");
                out.println("</ul>");

                out.println("<h2>🛏 No rooms appear in the reservation form</h2>");
                out.println("<ul>");
                out.println("<li>All rooms may currently be occupied or under maintenance.</li>");
                out.println("<li>Ask your administrator to check room statuses on the Rooms page.</li>");
                out.println("</ul>");

                out.println("<h2>💰 Bill already exists error</h2>");
                out.println("<ul>");
                out.println("<li>A bill has already been generated for this reservation.</li>");
                out.println("<li>Go to Billing and search for the reservation number to view the existing bill.</li>");
                out.println("</ul>");

                out.println("<h2>🗑 Cannot delete a room type</h2>");
                out.println("<ul>");
                out.println("<li>One or more rooms are still assigned to this room type.</li>");
                out.println("<li>Go to the Rooms tab, edit those rooms to use a different type, then try deleting again.</li>");
                out.println("</ul>");

                out.println("<h2>📞 Still need help?</h2>");
                out.println("<p>Contact your system administrator or the IT support team:</p>");
                out.println("<div class='info-box blue'>📧 <strong>IT Support:</strong> it@oceanviewresort.lk &nbsp;|&nbsp; 📞 <strong>Ext:</strong> 100</div>");
                out.println("</div>");
                break;
        }

        out.println("</div>"); // end help-content
        out.println("</div>"); // end main
        out.println("</body></html>");
    }

    // ------------------------------------------------
    // Helper - print help nav link
    // ------------------------------------------------
    private void printNavLink(PrintWriter out, String contextPath, String currentSection,
                              String sectionId, String icon, String label) {
        String activeClass = sectionId.equals(currentSection) ? " active" : "";
        out.println("<a href='" + contextPath + "/help?section=" + sectionId + "' class='" + activeClass + "'>" + icon + " " + label + "</a>");
    }

    // ------------------------------------------------
    // Helper - print role permission table row
    // ------------------------------------------------
    private void printRoleRow(PrintWriter out, String feature, boolean admin, boolean recep) {
        out.println("<tr>");
        out.println("<td>" + feature + "</td>");
        out.println("<td class='" + (admin ? "tick" : "cross") + "'>" + (admin ? "✔ Yes" : "✘ No") + "</td>");
        out.println("<td class='" + (recep ? "tick" : "cross") + "'>" + (recep ? "✔ Yes" : "✘ No") + "</td>");
        out.println("</tr>");
    }
}