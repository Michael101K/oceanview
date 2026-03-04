package lk.oceanview.servlet;

import com.google.gson.Gson;
import lk.oceanview.dao.GuestDAO;
import lk.oceanview.dao.ReservationDAO;
import lk.oceanview.dao.RoomDAO;
import lk.oceanview.model.Guest;
import lk.oceanview.model.Reservation;
import lk.oceanview.model.Room;
import lk.oceanview.model.User;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;

/**
 * ReservationServlet
 * GET  ?action=add    -> show add reservation form
 * GET  ?action=list   -> show all reservations
 * GET  ?action=view&reservationNumber=OVR-2025-0001 -> show single reservation
 * POST action=add     -> save new reservation
 * POST action=cancel  -> cancel a reservation
 */
public class ReservationServlet extends HttpServlet {

    private ReservationDAO reservationDAO;
    private RoomDAO        roomDAO;
    private GuestDAO       guestDAO;
    private Gson           gson;

    @Override
    public void init() throws ServletException {
        reservationDAO = new ReservationDAO();
        roomDAO        = new RoomDAO();
        guestDAO       = new GuestDAO();
        gson           = new Gson();
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

        String action = request.getParameter("action");
        if (action == null) action = "list";

        switch (action) {
            case "add":
                showAddForm(request, response, session);
                break;
            case "view":
                showReservationDetails(request, response, session);
                break;
            case "list":
            default:
                showAllReservations(request, response, session);
                break;
        }
    }

    // ------------------------------------------------
    // POST
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

        String action = request.getParameter("action");
        if (action == null) action = "add";

        switch (action) {
            case "add":
                addReservation(request, response, session);
                break;
            case "cancel":
                cancelReservation(request, response, session);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/reservation?action=list");
        }
    }

    // ------------------------------------------------
    // Show Add Reservation Form
    // ------------------------------------------------
    private void showAddForm(HttpServletRequest request, HttpServletResponse response,
                             HttpSession session) throws IOException {

        List<Room> availableRooms = roomDAO.getAvailableRooms();
        User user                 = (User) session.getAttribute("loggedInUser");
        String contextPath        = request.getContextPath();
        String msg                = request.getParameter("msg");
        String error              = request.getParameter("error");

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html><html lang='en'><head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("<title>New Reservation | Ocean View Resort</title>");
        out.println("<style>");
        out.println("* { margin:0; padding:0; box-sizing:border-box; }");
        out.println("body { font-family:'Segoe UI',sans-serif; background:#f0f4f8; }");

        // Sidebar
        out.println(".sidebar { width:240px; height:100vh; background:linear-gradient(180deg,#0f4c75,#1b6ca8); position:fixed; top:0; left:0; padding:30px 0; color:white; }");
        out.println(".sidebar .logo { text-align:center; padding:0 20px 30px; border-bottom:1px solid rgba(255,255,255,0.15); }");
        out.println(".sidebar .logo h2 { font-size:16px; font-weight:700; }");
        out.println(".sidebar nav a { display:block; padding:12px 25px; color:rgba(255,255,255,0.85); text-decoration:none; font-size:14px; transition:background 0.2s; }");
        out.println(".sidebar nav a:hover, .sidebar nav a.active { background:rgba(255,255,255,0.15); color:white; border-left:3px solid white; }");
        out.println(".sidebar .logout { position:absolute; bottom:20px; width:100%; padding:0 20px; }");
        out.println(".sidebar .logout a { display:block; padding:11px; text-align:center; background:rgba(255,255,255,0.15); color:white; border-radius:8px; text-decoration:none; font-size:13px; }");
        out.println(".sidebar .logout a:hover { background:rgba(255,80,80,0.4); }");

        // Main
        out.println(".main { margin-left:240px; padding:30px; }");
        out.println(".page-header { margin-bottom:24px; }");
        out.println(".page-header h1 { font-size:22px; color:#1a1a2e; font-weight:700; }");
        out.println(".page-header p { color:#888; font-size:13px; margin-top:4px; }");

        // Cards
        out.println(".card { background:white; border-radius:14px; padding:28px; box-shadow:0 2px 12px rgba(0,0,0,0.07); margin-bottom:24px; }");
        out.println(".card h2 { font-size:16px; font-weight:700; color:#1a1a2e; margin-bottom:20px; padding-bottom:12px; border-bottom:2px solid #f0f4f8; }");

        // Form
        out.println(".form-grid { display:grid; grid-template-columns:1fr 1fr; gap:18px; }");
        out.println(".form-grid.three { grid-template-columns:1fr 1fr 1fr; }");
        out.println(".form-group { display:flex; flex-direction:column; gap:6px; }");
        out.println(".form-group.full { grid-column:1/-1; }");
        out.println(".form-group label { font-size:13px; font-weight:600; color:#444; }");
        out.println(".form-group input, .form-group select, .form-group textarea { padding:11px 14px; border:1.5px solid #e0e0e0; border-radius:10px; font-size:14px; color:#333; outline:none; transition:border-color 0.3s; font-family:inherit; }");
        out.println(".form-group input:focus, .form-group select:focus, .form-group textarea:focus { border-color:#1b6ca8; box-shadow:0 0 0 3px rgba(27,108,168,0.1); }");
        out.println(".form-group input[readonly] { background:#f8f9fa; color:#666; }");

        // Room info box
        out.println(".room-info { background:#f0f7ff; border:1px solid #bee3f8; border-radius:10px; padding:14px 18px; margin-top:8px; display:none; font-size:13px; color:#2c5f8a; }");
        out.println(".room-info span { font-weight:700; }");

        // Buttons
        out.println(".btn { padding:12px 28px; border:none; border-radius:10px; font-size:14px; font-weight:600; cursor:pointer; transition:transform 0.2s,box-shadow 0.2s; }");
        out.println(".btn-primary { background:linear-gradient(135deg,#0f4c75,#1b6ca8); color:white; }");
        out.println(".btn-primary:hover { transform:translateY(-2px); box-shadow:0 6px 18px rgba(27,108,168,0.35); }");
        out.println(".btn-secondary { background:#f0f4f8; color:#555; margin-left:10px; }");
        out.println(".btn-secondary:hover { background:#e2e8f0; }");

        // Alerts
        out.println(".alert { padding:12px 18px; border-radius:10px; font-size:13px; margin-bottom:20px; }");
        out.println(".alert-success { background:#f0fdf4; color:#16a34a; border:1px solid #bbf7d0; }");
        out.println(".alert-error   { background:#fef2f2; color:#dc2626; border:1px solid #fecaca; }");

        // NIC lookup badge
        out.println(".nic-badge { font-size:11px; padding:3px 10px; border-radius:20px; margin-left:8px; font-weight:600; }");
        out.println(".nic-found { background:#d1fae5; color:#065f46; }");
        out.println(".nic-new   { background:#dbeafe; color:#1e40af; }");

        out.println("</style></head><body>");

        // Sidebar
        out.println("<div class='sidebar'>");
        out.println("<div class='logo'><h2>🏨 Ocean View</h2></div><nav>");
        out.println("<a href='" + contextPath + "/dashboard'>🏠 Dashboard</a>");
        out.println("<a href='" + contextPath + "/reservation?action=add' class='active'>➕ New Reservation</a>");
        out.println("<a href='" + contextPath + "/reservation?action=list'>📋 All Reservations</a>");
        out.println("<a href='" + contextPath + "/room?action=list'>🛏 Rooms</a>");
        out.println("<a href='" + contextPath + "/bill'>💰 Billing</a>");
        if ("ADMIN".equals(role)) {
            out.println("<a href='" + contextPath + "/user?action=list'>👥 Manage Users</a>");
        }
        out.println("</nav>");
        out.println("<div class='logout'><a href='" + contextPath + "/logout'>🚪 Logout</a></div></div>");

        // Main
        out.println("<div class='main'>");
        out.println("<div class='page-header'><h1>➕ New Reservation</h1><p>Fill in the guest and booking details below</p></div>");

        // Alert messages
        if (msg != null && msg.equals("success")) {
            out.println("<div class='alert alert-success'>✅ Reservation created successfully!</div>");
        }
        if (error != null) {
            String errMsg = error.equals("dates") ? "Check-out date must be after check-in date." :
                            error.equals("room")  ? "Selected room is not available for those dates." :
                            error.equals("guest") ? "Failed to save guest details. Please try again." :
                            "Something went wrong. Please try again.";
            out.println("<div class='alert alert-error'>⚠ " + errMsg + "</div>");
        }

        // Form
        out.println("<form method='POST' action='" + contextPath + "/reservation' id='reservationForm'>");
        out.println("<input type='hidden' name='action' value='add'>");

        // --- Guest Details Card ---
        out.println("<div class='card'>");
        out.println("<h2>👤 Guest Details</h2>");
        out.println("<div class='form-group' style='margin-bottom:18px;'>");
        out.println("<label>NIC / Passport Number <span id='nicBadge' class='nic-badge' style='display:none'></span></label>");
        out.println("<input type='text' id='nicNumber' name='nicNumber' placeholder='Enter NIC to auto-fill guest details' oninput='lookupGuest(this.value)' />");
        out.println("</div>");
        out.println("<div class='form-grid'>");
        out.println("<input type='hidden' id='existingGuestId' name='existingGuestId' value=''>");
        out.println("<div class='form-group'><label>Full Name *</label><input type='text' id='fullName' name='fullName' placeholder='e.g. Kasun Perera' required /></div>");
        out.println("<div class='form-group'><label>Contact Number *</label><input type='text' id='contactNumber' name='contactNumber' placeholder='e.g. 0771234567' required /></div>");
        out.println("<div class='form-group full'><label>Address *</label><input type='text' id='address' name='address' placeholder='e.g. No 45, Galle Road, Colombo' required /></div>");
        out.println("<div class='form-group'><label>Email</label><input type='email' id='email' name='email' placeholder='e.g. guest@email.com' /></div>");
        out.println("</div></div>"); // end guest card

        // --- Booking Details Card ---
        out.println("<div class='card'>");
        out.println("<h2>📅 Booking Details</h2>");
        out.println("<div class='form-grid'>");

        // Room dropdown
        out.println("<div class='form-group'><label>Select Room *</label>");
        out.println("<select id='roomSelect' name='roomId' required onchange='showRoomInfo(this)'>");
        out.println("<option value=''>-- Select an available room --</option>");
        for (Room room : availableRooms) {
            out.println("<option value='" + room.getRoomId() + "' " +
                        "data-rate='" + room.getRatePerNight() + "' " +
                        "data-type='" + room.getRoomTypeName() + "'>" +
                        "Room " + room.getRoomNumber() + " - " + room.getRoomTypeName() +
                        " (LKR " + String.format("%,.0f", room.getRatePerNight()) + "/night)</option>");
        }
        out.println("</select>");
        out.println("<div class='room-info' id='roomInfo'>Room Type: <span id='roomType'></span> &nbsp;|&nbsp; Rate: LKR <span id='roomRate'></span>/night</div>");
        out.println("</div>"); // end room form-group

        // Check-in / Check-out
        String today    = LocalDate.now().toString();
        String tomorrow = LocalDate.now().plusDays(1).toString();
        out.println("<div class='form-group'></div>"); // spacer
        out.println("<div class='form-group'><label>Check-in Date *</label><input type='date' id='checkIn' name='checkInDate' value='" + today + "' min='" + today + "' required onchange='calculateNights()' /></div>");
        out.println("<div class='form-group'><label>Check-out Date *</label><input type='date' id='checkOut' name='checkOutDate' value='" + tomorrow + "' min='" + tomorrow + "' required onchange='calculateNights()' /></div>");

        // Nights + Total (readonly, auto-calculated)
        out.println("<div class='form-group'><label>Number of Nights</label><input type='text' id='numNights' readonly placeholder='Auto calculated' /></div>");
        out.println("<div class='form-group'><label>Estimated Total (LKR)</label><input type='text' id='totalAmount' name='totalAmount' readonly placeholder='Auto calculated' /></div>");

        // Special requests
        out.println("<div class='form-group full'><label>Special Requests</label><textarea name='specialRequests' rows='2' placeholder='Any special requests from the guest...'></textarea></div>");
        out.println("</div></div>"); // end booking card

        // Buttons
        out.println("<div style='margin-top:4px;'>");
        out.println("<button type='submit' class='btn btn-primary'>✅ Confirm Reservation</button>");
        out.println("<a href='" + contextPath + "/dashboard'><button type='button' class='btn btn-secondary'>Cancel</button></a>");
        out.println("</div>");

        out.println("</form></div>"); // end main

        // JavaScript
        out.println("<script>");
        out.println("const contextPath = '" + contextPath + "';");
        out.println("let lookupTimeout = null;");

        // NIC auto-lookup
        out.println("function lookupGuest(nic) {");
        out.println("  clearTimeout(lookupTimeout);");
        out.println("  if (nic.length < 5) { clearGuestFields(); return; }");
        out.println("  lookupTimeout = setTimeout(() => {");
        out.println("    fetch(contextPath + '/guest?action=lookup&nic=' + encodeURIComponent(nic))");
        out.println("      .then(r => r.json()).then(data => {");
        out.println("        const badge = document.getElementById('nicBadge');");
        out.println("        if (data.found) {");
        out.println("          document.getElementById('fullName').value       = data.fullName;");
        out.println("          document.getElementById('address').value        = data.address;");
        out.println("          document.getElementById('contactNumber').value  = data.contactNumber;");
        out.println("          document.getElementById('email').value          = data.email || '';");
        out.println("          document.getElementById('existingGuestId').value = data.guestId;");
        out.println("          badge.textContent = '✓ Returning Guest'; badge.className = 'nic-badge nic-found'; badge.style.display='inline';");
        out.println("        } else {");
        out.println("          clearGuestFields();");
        out.println("          badge.textContent = '+ New Guest'; badge.className = 'nic-badge nic-new'; badge.style.display='inline';");
        out.println("        }");
        out.println("      }).catch(() => clearGuestFields());");
        out.println("  }, 500);");
        out.println("}");

        out.println("function clearGuestFields() {");
        out.println("  ['fullName','address','contactNumber','email'].forEach(id => document.getElementById(id).value = '');");
        out.println("  document.getElementById('existingGuestId').value = '';");
        out.println("  document.getElementById('nicBadge').style.display = 'none';");
        out.println("}");

        // Room info display
        out.println("function showRoomInfo(sel) {");
        out.println("  const opt = sel.options[sel.selectedIndex];");
        out.println("  const info = document.getElementById('roomInfo');");
        out.println("  if (sel.value) {");
        out.println("    document.getElementById('roomType').textContent = opt.dataset.type;");
        out.println("    document.getElementById('roomRate').textContent = parseFloat(opt.dataset.rate).toLocaleString();");
        out.println("    info.style.display = 'block';");
        out.println("    calculateNights();");
        out.println("  } else { info.style.display = 'none'; }");
        out.println("}");

        // Calculate nights and total
        out.println("function calculateNights() {");
        out.println("  const ci = document.getElementById('checkIn').value;");
        out.println("  const co = document.getElementById('checkOut').value;");
        out.println("  const sel = document.getElementById('roomSelect');");
        out.println("  if (ci && co) {");
        out.println("    const diff = (new Date(co) - new Date(ci)) / 86400000;");
        out.println("    document.getElementById('numNights').value = diff > 0 ? diff + ' night(s)' : 'Invalid dates';");
        out.println("    if (diff > 0 && sel.value) {");
        out.println("      const rate = parseFloat(sel.options[sel.selectedIndex].dataset.rate);");
        out.println("      document.getElementById('totalAmount').value = (diff * rate).toLocaleString('en-LK', {minimumFractionDigits:2});");
        out.println("    }");
        out.println("  }");
        out.println("}");

        // Set min checkout date when checkin changes
        out.println("document.getElementById('checkIn').addEventListener('change', function() {");
        out.println("  const next = new Date(this.value); next.setDate(next.getDate()+1);");
        out.println("  document.getElementById('checkOut').min = next.toISOString().split('T')[0];");
        out.println("  if (document.getElementById('checkOut').value <= this.value) {");
        out.println("    document.getElementById('checkOut').value = next.toISOString().split('T')[0];");
        out.println("  }");
        out.println("  calculateNights();");
        out.println("});");

        out.println("</script></body></html>");
    }

    // ------------------------------------------------
    // Process Add Reservation (POST)
    // ------------------------------------------------
    private void addReservation(HttpServletRequest request, HttpServletResponse response,
                                HttpSession session) throws IOException {

        User user = (User) session.getAttribute("loggedInUser");
        String contextPath = request.getContextPath();

        try {
            // Get form values
            String existingGuestId = request.getParameter("existingGuestId");
            String nicNumber       = request.getParameter("nicNumber");
            String fullName        = request.getParameter("fullName");
            String address         = request.getParameter("address");
            String contactNumber   = request.getParameter("contactNumber");
            String email           = request.getParameter("email");
            int    roomId          = Integer.parseInt(request.getParameter("roomId"));
            String checkInStr      = request.getParameter("checkInDate");
            String checkOutStr     = request.getParameter("checkOutDate");
            String specialRequests = request.getParameter("specialRequests");
            String totalAmountStr  = request.getParameter("totalAmount");

            // Parse dates
            LocalDate checkIn  = LocalDate.parse(checkInStr);
            LocalDate checkOut = LocalDate.parse(checkOutStr);

            // Validate dates
            if (!checkOut.isAfter(checkIn)) {
                response.sendRedirect(contextPath + "/reservation?action=add&error=dates");
                return;
            }

            // Check room availability
            if (!roomDAO.isRoomAvailableForDates(roomId, checkInStr, checkOutStr)) {
                response.sendRedirect(contextPath + "/reservation?action=add&error=room");
                return;
            }

            // Get or create guest
            int guestId;
            if (existingGuestId != null && !existingGuestId.trim().isEmpty()) {
                guestId = Integer.parseInt(existingGuestId.trim());
            } else {
                Guest guest = new Guest();
                guest.setFullName(fullName.trim());
                guest.setAddress(address.trim());
                guest.setContactNumber(contactNumber.trim());
                guest.setEmail(email != null ? email.trim() : "");
                guest.setNicNumber(nicNumber != null ? nicNumber.trim() : "");
                guestId = guestDAO.addGuest(guest);
                if (guestId < 0) {
                    response.sendRedirect(contextPath + "/reservation?action=add&error=guest");
                    return;
                }
            }

            // Calculate total
            Room room = roomDAO.getRoomById(roomId);
            long nights = java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
            double total = nights * room.getRatePerNight();

            // Build reservation
            Reservation reservation = new Reservation();
            reservation.setReservationNumber(reservationDAO.generateReservationNumber());
            reservation.setGuestId(guestId);
            reservation.setRoomId(roomId);
            reservation.setCheckInDate(checkIn);
            reservation.setCheckOutDate(checkOut);
            reservation.setTotalAmount(total);
            reservation.setStatus("CONFIRMED");
            reservation.setSpecialRequests(specialRequests);
            reservation.setCreatedBy(user.getUserId());

            // Save reservation
            int reservationId = reservationDAO.addReservation(reservation);
            if (reservationId > 0) {
                // Mark room as occupied
                roomDAO.updateRoomStatus(roomId, "OCCUPIED");
                response.sendRedirect(contextPath + "/reservation?action=add&msg=success");
            } else {
                response.sendRedirect(contextPath + "/reservation?action=add&error=save");
            }

        } catch (Exception e) {
            System.err.println("[ReservationServlet] addReservation error: " + e.getMessage());
            response.sendRedirect(contextPath + "/reservation?action=add&error=save");
        }
    }

    // ------------------------------------------------
    // Show All Reservations
    // ------------------------------------------------
    private void showAllReservations(HttpServletRequest request, HttpServletResponse response,
                                     HttpSession session) throws IOException {

        List<Reservation> reservations = reservationDAO.getAllReservations();
        String contextPath = request.getContextPath();
        User user = (User) session.getAttribute("loggedInUser");

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html><html lang='en'><head>");
        out.println("<meta charset='UTF-8'><title>All Reservations | Ocean View Resort</title>");
        out.println("<style>");
        out.println("* { margin:0; padding:0; box-sizing:border-box; }");
        out.println("body { font-family:'Segoe UI',sans-serif; background:#f0f4f8; }");
        out.println(".sidebar { width:240px; height:100vh; background:linear-gradient(180deg,#0f4c75,#1b6ca8); position:fixed; top:0; left:0; padding:30px 0; color:white; }");
        out.println(".sidebar .logo { text-align:center; padding:0 20px 30px; border-bottom:1px solid rgba(255,255,255,0.15); }");
        out.println(".sidebar .logo h2 { font-size:16px; font-weight:700; }");
        out.println(".sidebar nav a { display:block; padding:12px 25px; color:rgba(255,255,255,0.85); text-decoration:none; font-size:14px; }");
        out.println(".sidebar nav a:hover, .sidebar nav a.active { background:rgba(255,255,255,0.15); border-left:3px solid white; }");
        out.println(".sidebar .logout { position:absolute; bottom:20px; width:100%; padding:0 20px; }");
        out.println(".sidebar .logout a { display:block; padding:11px; text-align:center; background:rgba(255,255,255,0.15); color:white; border-radius:8px; text-decoration:none; font-size:13px; }");
        out.println(".main { margin-left:240px; padding:30px; }");
        out.println(".topbar { display:flex; justify-content:space-between; align-items:center; margin-bottom:24px; }");
        out.println(".topbar h1 { font-size:22px; color:#1a1a2e; font-weight:700; }");
        out.println(".btn { padding:10px 20px; border:none; border-radius:8px; font-size:13px; font-weight:600; cursor:pointer; text-decoration:none; }");
        out.println(".btn-primary { background:linear-gradient(135deg,#0f4c75,#1b6ca8); color:white; }");
        out.println(".search-box { padding:10px 16px; border:1.5px solid #e0e0e0; border-radius:10px; font-size:14px; width:280px; outline:none; }");
        out.println(".search-box:focus { border-color:#1b6ca8; }");
        out.println(".card { background:white; border-radius:14px; padding:24px; box-shadow:0 2px 12px rgba(0,0,0,0.07); }");
        out.println("table { width:100%; border-collapse:collapse; }");
        out.println("th { background:#f8fafc; padding:12px 16px; text-align:left; font-size:12px; font-weight:700; color:#666; text-transform:uppercase; letter-spacing:0.5px; border-bottom:2px solid #f0f4f8; }");
        out.println("td { padding:14px 16px; font-size:13px; color:#333; border-bottom:1px solid #f8fafc; }");
        out.println("tr:hover td { background:#fafbff; }");
        out.println(".badge { padding:4px 12px; border-radius:20px; font-size:11px; font-weight:700; }");
        out.println(".badge-confirmed  { background:#dbeafe; color:#1e40af; }");
        out.println(".badge-checkedin  { background:#d1fae5; color:#065f46; }");
        out.println(".badge-checkedout { background:#f3f4f6; color:#6b7280; }");
        out.println(".badge-cancelled  { background:#fee2e2; color:#991b1b; }");
        out.println(".empty { text-align:center; padding:50px; color:#aaa; font-size:15px; }");
        out.println("</style></head><body>");

        // Sidebar
        out.println("<div class='sidebar'><div class='logo'><h2>🏨 Ocean View</h2></div><nav>");
        out.println("<a href='" + contextPath + "/dashboard'>🏠 Dashboard</a>");
        out.println("<a href='" + contextPath + "/reservation?action=add'>➕ New Reservation</a>");
        out.println("<a href='" + contextPath + "/reservation?action=list' class='active'>📋 All Reservations</a>");
        out.println("<a href='" + contextPath + "/room?action=list'>🛏 Rooms</a>");
        out.println("<a href='" + contextPath + "/bill'>💰 Billing</a>");
        if ("ADMIN".equals(role)) {
            out.println("<a href='" + contextPath + "/user?action=list'>👥 Manage Users</a>");
        }
        out.println("</nav><div class='logout'><a href='" + contextPath + "/logout'>🚪 Logout</a></div></div>");

        // Main
        out.println("<div class='main'>");
        out.println("<div class='topbar'>");
        out.println("<h1>📋 All Reservations</h1>");
        out.println("<div style='display:flex;gap:12px;align-items:center;'>");
        out.println("<input class='search-box' type='text' id='searchInput' placeholder='🔍 Search by name or reservation no...' oninput='filterTable(this.value)'>");
        out.println("<a href='" + contextPath + "/reservation?action=add' class='btn btn-primary'>➕ New Reservation</a>");
        out.println("</div></div>");

        out.println("<div class='card'>");

        if (reservations.isEmpty()) {
            out.println("<div class='empty'>📭 No reservations found. <a href='" + contextPath + "/reservation?action=add'>Add the first one!</a></div>");
        } else {
            out.println("<table id='reservationTable'><thead><tr>");
            out.println("<th>Reservation #</th><th>Guest Name</th><th>Room</th><th>Check-In</th><th>Check-Out</th><th>Nights</th><th>Total (LKR)</th><th>Status</th><th>Actions</th>");
            out.println("</tr></thead><tbody>");

            for (Reservation r : reservations) {
                String badgeClass = "badge-" + r.getStatus().toLowerCase().replace("_", "");
                out.println("<tr>");
                out.println("<td><strong>" + r.getReservationNumber() + "</strong></td>");
                out.println("<td>" + r.getGuestName() + "</td>");
                out.println("<td>" + r.getRoomNumber() + " (" + r.getRoomTypeName() + ")</td>");
                out.println("<td>" + r.getCheckInDate() + "</td>");
                out.println("<td>" + r.getCheckOutDate() + "</td>");
                out.println("<td>" + r.getNumNights() + "</td>");
                out.println("<td>" + String.format("%,.2f", r.getTotalAmount()) + "</td>");
                out.println("<td><span class='badge " + badgeClass + "'>" + r.getStatus() + "</span></td>");
                out.println("<td><a href='" + contextPath + "/reservation?action=view&reservationNumber=" + r.getReservationNumber() + "' style='color:#1b6ca8;text-decoration:none;font-weight:600;font-size:12px;'>View</a></td>");
                out.println("</tr>");
            }
            out.println("</tbody></table>");
        }

        out.println("</div></div>");

        // Search filter script
        out.println("<script>");
        out.println("function filterTable(query) {");
        out.println("  const rows = document.querySelectorAll('#reservationTable tbody tr');");
        out.println("  rows.forEach(row => {");
        out.println("    const text = row.textContent.toLowerCase();");
        out.println("    row.style.display = text.includes(query.toLowerCase()) ? '' : 'none';");
        out.println("  });");
        out.println("}");
        out.println("</script></body></html>");
    }

    // ------------------------------------------------
    // Show Single Reservation Details
    // ------------------------------------------------
    private void showReservationDetails(HttpServletRequest request, HttpServletResponse response,
                                        HttpSession session) throws IOException {

        String reservationNumber = request.getParameter("reservationNumber");
        String contextPath       = request.getContextPath();

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        Reservation r = null;
        if (reservationNumber != null) {
            r = reservationDAO.getReservationByNumber(reservationNumber.trim());
        }

        out.println("<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'>");
        out.println("<title>Reservation Details | Ocean View Resort</title>");
        out.println("<style>");
        out.println("* { margin:0; padding:0; box-sizing:border-box; }");
        out.println("body { font-family:'Segoe UI',sans-serif; background:#f0f4f8; }");
        out.println(".sidebar { width:240px; height:100vh; background:linear-gradient(180deg,#0f4c75,#1b6ca8); position:fixed; top:0; left:0; padding:30px 0; }");
        out.println(".sidebar .logo { text-align:center; padding:0 20px 30px; border-bottom:1px solid rgba(255,255,255,0.15); }");
        out.println(".sidebar .logo h2 { font-size:16px; font-weight:700; color:white; }");
        out.println(".sidebar nav a { display:block; padding:12px 25px; color:rgba(255,255,255,0.85); text-decoration:none; font-size:14px; }");
        out.println(".sidebar nav a:hover { background:rgba(255,255,255,0.15); border-left:3px solid white; }");
        out.println(".sidebar .logout { position:absolute; bottom:20px; width:100%; padding:0 20px; }");
        out.println(".sidebar .logout a { display:block; padding:11px; text-align:center; background:rgba(255,255,255,0.15); color:white; border-radius:8px; text-decoration:none; font-size:13px; }");
        out.println(".main { margin-left:240px; padding:30px; }");
        out.println(".card { background:white; border-radius:14px; padding:28px; box-shadow:0 2px 12px rgba(0,0,0,0.07); max-width:700px; }");
        out.println(".card h2 { font-size:18px; font-weight:700; color:#1a1a2e; margin-bottom:20px; padding-bottom:12px; border-bottom:2px solid #f0f4f8; }");
        out.println(".detail-grid { display:grid; grid-template-columns:1fr 1fr; gap:16px; }");
        out.println(".detail-item label { font-size:12px; color:#888; font-weight:600; text-transform:uppercase; display:block; margin-bottom:4px; }");
        out.println(".detail-item p { font-size:14px; color:#1a1a2e; font-weight:500; }");
        out.println(".badge { padding:5px 14px; border-radius:20px; font-size:12px; font-weight:700; }");
        out.println(".badge-confirmed { background:#dbeafe; color:#1e40af; }");
        out.println(".badge-checkedin { background:#d1fae5; color:#065f46; }");
        out.println(".badge-checkedout { background:#f3f4f6; color:#6b7280; }");
        out.println(".badge-cancelled { background:#fee2e2; color:#991b1b; }");
        out.println(".btn { padding:10px 22px; border:none; border-radius:8px; font-size:13px; font-weight:600; cursor:pointer; text-decoration:none; margin-right:10px; display:inline-block; margin-top:20px; }");
        out.println(".btn-primary { background:linear-gradient(135deg,#0f4c75,#1b6ca8); color:white; }");
        out.println(".btn-secondary { background:#f0f4f8; color:#555; }");
        out.println(".not-found { text-align:center; padding:60px; color:#aaa; }");
        out.println("</style></head><body>");

        // Sidebar
        out.println("<div class='sidebar'><div class='logo'><h2>🏨 Ocean View</h2></div><nav>");
        out.println("<a href='" + contextPath + "/dashboard'>🏠 Dashboard</a>");
        out.println("<a href='" + contextPath + "/reservation?action=add'>➕ New Reservation</a>");
        out.println("<a href='" + contextPath + "/reservation?action=list' class='active'>📋 All Reservations</a>");
        out.println("<a href='" + contextPath + "/room?action=list'>🛏 Rooms</a>");
        out.println("<a href='" + contextPath + "/bill'>💰 Billing</a>");
        out.println("</nav><div class='logout'><a href='" + contextPath + "/logout'>🚪 Logout</a></div></div>");

        out.println("<div class='main'>");

        if (r == null) {
            out.println("<div class='card'><div class='not-found'>❌ Reservation not found.<br><br>");
            out.println("<a href='" + contextPath + "/reservation?action=list' class='btn btn-secondary'>← Back to List</a></div></div>");
        } else {
            String badgeClass = "badge-" + r.getStatus().toLowerCase().replace("_", "");
            out.println("<div class='card'>");
            out.println("<h2>🧾 Reservation: " + r.getReservationNumber() + " &nbsp;<span class='badge " + badgeClass + "'>" + r.getStatus() + "</span></h2>");
            out.println("<div class='detail-grid'>");
            out.println("<div class='detail-item'><label>Guest Name</label><p>" + r.getGuestName() + "</p></div>");
            out.println("<div class='detail-item'><label>Room</label><p>Room " + r.getRoomNumber() + " — " + r.getRoomTypeName() + "</p></div>");
            out.println("<div class='detail-item'><label>Check-in Date</label><p>" + r.getCheckInDate() + "</p></div>");
            out.println("<div class='detail-item'><label>Check-out Date</label><p>" + r.getCheckOutDate() + "</p></div>");
            out.println("<div class='detail-item'><label>Number of Nights</label><p>" + r.getNumNights() + " night(s)</p></div>");
            out.println("<div class='detail-item'><label>Rate Per Night</label><p>LKR " + String.format("%,.2f", r.getRatePerNight()) + "</p></div>");
            out.println("<div class='detail-item'><label>Total Amount</label><p><strong>LKR " + String.format("%,.2f", r.getTotalAmount()) + "</strong></p></div>");
            out.println("<div class='detail-item'><label>Special Requests</label><p>" + (r.getSpecialRequests() != null && !r.getSpecialRequests().isEmpty() ? r.getSpecialRequests() : "None") + "</p></div>");
            out.println("</div>");
            out.println("<a href='" + contextPath + "/bill?reservationNumber=" + r.getReservationNumber() + "' class='btn btn-primary'>🧾 Generate Bill</a>");
            out.println("<a href='" + contextPath + "/reservation?action=list' class='btn btn-secondary'>← Back</a>");
            out.println("</div>");
        }

        out.println("</div></body></html>");
    }

    // ------------------------------------------------
    // Cancel Reservation (POST)
    // ------------------------------------------------
    private void cancelReservation(HttpServletRequest request, HttpServletResponse response,
                                   HttpSession session) throws IOException {
        String contextPath   = request.getContextPath();
        String idStr         = request.getParameter("reservationId");
        try {
            int reservationId = Integer.parseInt(idStr);
            reservationDAO.cancelReservation(reservationId);
        } catch (Exception e) {
            System.err.println("[ReservationServlet] cancelReservation error: " + e.getMessage());
        }
        response.sendRedirect(contextPath + "/reservation?action=list");
    }
}