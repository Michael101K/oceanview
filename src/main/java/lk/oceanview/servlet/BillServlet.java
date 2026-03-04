package lk.oceanview.servlet;

import lk.oceanview.dao.BillDAO;
import lk.oceanview.dao.ReservationDAO;
import lk.oceanview.dao.RoomDAO;
import lk.oceanview.model.Bill;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * BillServlet
 * GET  ?reservationNumber=OVR-2025-0001  -> show bill for a reservation
 * POST action=generate                   -> generate and save bill to DB
 * POST action=markPaid                   -> update payment status to PAID
 */
public class BillServlet extends HttpServlet {

    private BillDAO        billDAO;
    private ReservationDAO reservationDAO;
    private RoomDAO        roomDAO;

    @Override
    public void init() throws ServletException {
        billDAO        = new BillDAO();
        reservationDAO = new ReservationDAO();
        roomDAO        = new RoomDAO();
    }

    // ------------------------------------------------
    // GET - show bill page
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

        String reservationNumber = request.getParameter("reservationNumber");
        String contextPath       = request.getContextPath();

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        // If no reservation number given - show search form
        if (reservationNumber == null || reservationNumber.trim().isEmpty()) {
            showBillSearchPage(out, contextPath, null, null);
            return;
        }

        // Look up reservation
        Reservation reservation = reservationDAO.getReservationByNumber(reservationNumber.trim());
        if (reservation == null) {
            showBillSearchPage(out, contextPath, reservationNumber, "notfound");
            return;
        }

        // Check if bill already exists
        // We need reservation_id - fetch basic reservation
        // Get it via the view data we already have
        Bill existingBill = null;

        // Find reservation_id by joining manually
        String sql = "SELECT reservation_id FROM reservations WHERE reservation_number = ?";
        int reservationId = -1;
        try (java.sql.Connection conn = lk.oceanview.config.DBConnection.getInstance().getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, reservationNumber.trim());
            java.sql.ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                reservationId = rs.getInt("reservation_id");
            }
        } catch (Exception e) {
            System.err.println("[BillServlet] Error fetching reservation_id: " + e.getMessage());
        }

        if (reservationId > 0) {
            existingBill = billDAO.getBillByReservationId(reservationId);
        }

        // Show bill page
        showBillPage(out, contextPath, reservation, existingBill, reservationId);
    }

    // ------------------------------------------------
    // POST - generate bill or mark as paid
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

        String action      = request.getParameter("action");
        String contextPath = request.getContextPath();

        if ("generate".equals(action)) {
            generateBill(request, response, session, contextPath);
        } else if ("markPaid".equals(action)) {
            markAsPaid(request, response, contextPath);
        } else {
            response.sendRedirect(contextPath + "/bill");
        }
    }

    // ------------------------------------------------
    // Generate and save bill
    // ------------------------------------------------
    private void generateBill(HttpServletRequest request, HttpServletResponse response,
                               HttpSession session, String contextPath) throws IOException {
        try {
            int    reservationId   = Integer.parseInt(request.getParameter("reservationId"));
            long   numNights       = Long.parseLong(request.getParameter("numNights"));
            double ratePerNight    = Double.parseDouble(request.getParameter("ratePerNight"));
            double discountAmount  = 0;
            String discountStr     = request.getParameter("discountAmount");
            if (discountStr != null && !discountStr.trim().isEmpty()) {
                discountAmount = Double.parseDouble(discountStr);
            }
            String paymentMethod   = request.getParameter("paymentMethod");
            String reservationNumber = request.getParameter("reservationNumber");

            // Calculate bill
            Bill bill = Bill.calculateBill(reservationId, numNights, ratePerNight,
                                           discountAmount, paymentMethod);

            // Save to DB
            int billId = billDAO.addBill(bill);
            if (billId > 0) {
                response.sendRedirect(contextPath + "/bill?reservationNumber=" + reservationNumber + "&msg=generated");
            } else {
                response.sendRedirect(contextPath + "/bill?reservationNumber=" + reservationNumber + "&error=save");
            }

        } catch (Exception e) {
            System.err.println("[BillServlet] generateBill error: " + e.getMessage());
            response.sendRedirect(contextPath + "/bill?error=save");
        }
    }

    // ------------------------------------------------
    // Mark bill as paid
    // ------------------------------------------------
    private void markAsPaid(HttpServletRequest request, HttpServletResponse response,
                            String contextPath) throws IOException {
        try {
            int    billId            = Integer.parseInt(request.getParameter("billId"));
            String reservationNumber = request.getParameter("reservationNumber");
            billDAO.updatePaymentStatus(billId, "PAID");
            response.sendRedirect(contextPath + "/bill?reservationNumber=" + reservationNumber + "&msg=paid");
        } catch (Exception e) {
            System.err.println("[BillServlet] markAsPaid error: " + e.getMessage());
            response.sendRedirect(contextPath + "/bill");
        }
    }

    // ------------------------------------------------
    // Show Bill Search Page
    // ------------------------------------------------
    private void showBillSearchPage(PrintWriter out, String contextPath,
                                    String searchedNumber, String error) {
        printPageHeader(out, "Billing | Ocean View Resort", contextPath);

        out.println("<div class='main'>");
        out.println("<div class='page-header'><h1>💰 Billing</h1><p>Enter a reservation number to generate or view a bill</p></div>");

        if ("notfound".equals(error)) {
            out.println("<div class='alert alert-error'>⚠ Reservation <strong>" + searchedNumber + "</strong> not found. Please check the number and try again.</div>");
        }

        out.println("<div class='card' style='max-width:500px;'>");
        out.println("<h2>🔍 Find Reservation</h2>");
        out.println("<form method='GET' action='" + contextPath + "/bill'>");
        out.println("<div class='form-group'>");
        out.println("<label>Reservation Number</label>");
        out.println("<input type='text' name='reservationNumber' placeholder='e.g. OVR-2025-0001' " +
                    "value='" + (searchedNumber != null ? searchedNumber : "") + "' required " +
                    "style='padding:12px 16px;border:1.5px solid #e0e0e0;border-radius:10px;font-size:14px;width:100%;outline:none;margin-top:6px;' />");
        out.println("</div>");
        out.println("<button type='submit' class='btn btn-primary' style='margin-top:16px;'>Find Reservation</button>");
        out.println("</form>");
        out.println("</div>");

        // Recent reservations list for quick access
        out.println("<div class='card' style='margin-top:24px;'>");
        out.println("<h2>📋 Recent Reservations</h2>");
        out.println("<p style='font-size:13px;color:#888;'>Click on a reservation to generate its bill.</p>");
        out.println("<div style='margin-top:16px;'>");

        ReservationDAO rDAO = new ReservationDAO();
        java.util.List<lk.oceanview.model.Reservation> recent = rDAO.getAllReservations();
        if (recent.isEmpty()) {
            out.println("<p style='color:#aaa;text-align:center;padding:20px;'>No reservations found.</p>");
        } else {
            out.println("<table style='width:100%;border-collapse:collapse;'>");
            out.println("<thead><tr>");
            out.println("<th style='padding:10px 14px;text-align:left;font-size:12px;color:#666;font-weight:700;text-transform:uppercase;border-bottom:2px solid #f0f4f8;'>Reservation #</th>");
            out.println("<th style='padding:10px 14px;text-align:left;font-size:12px;color:#666;font-weight:700;text-transform:uppercase;border-bottom:2px solid #f0f4f8;'>Guest</th>");
            out.println("<th style='padding:10px 14px;text-align:left;font-size:12px;color:#666;font-weight:700;text-transform:uppercase;border-bottom:2px solid #f0f4f8;'>Status</th>");
            out.println("<th style='padding:10px 14px;text-align:left;font-size:12px;color:#666;font-weight:700;text-transform:uppercase;border-bottom:2px solid #f0f4f8;'>Action</th>");
            out.println("</tr></thead><tbody>");
            for (lk.oceanview.model.Reservation r : recent) {
                String badgeClass = getBadgeClass(r.getStatus());
                out.println("<tr>");
                out.println("<td style='padding:12px 14px;font-size:13px;border-bottom:1px solid #f8fafc;'><strong>" + r.getReservationNumber() + "</strong></td>");
                out.println("<td style='padding:12px 14px;font-size:13px;border-bottom:1px solid #f8fafc;'>" + r.getGuestName() + "</td>");
                out.println("<td style='padding:12px 14px;font-size:13px;border-bottom:1px solid #f8fafc;'><span class='badge " + badgeClass + "'>" + r.getStatus() + "</span></td>");
                out.println("<td style='padding:12px 14px;font-size:13px;border-bottom:1px solid #f8fafc;'>");
                out.println("<a href='" + contextPath + "/bill?reservationNumber=" + r.getReservationNumber() + "' style='color:#1b6ca8;font-weight:600;text-decoration:none;font-size:12px;'>Generate Bill →</a>");
                out.println("</td></tr>");
            }
            out.println("</tbody></table>");
        }
        out.println("</div></div></div>");
        printPageFooter(out);
    }

    // ------------------------------------------------
    // Show Bill Details Page
    // ------------------------------------------------
    private void showBillPage(PrintWriter out, String contextPath,
                              Reservation reservation, Bill existingBill,
                              int reservationId) {

        printPageHeader(out, "Bill | Ocean View Resort", contextPath);

        String msg   = ""; // passed via redirect in real flow
        long nights  = reservation.getNumNights();
        double rate  = reservation.getRatePerNight();
        double rooms = nights * rate;
        double tax   = rooms * Bill.TAX_RATE;
        double total = rooms + tax;

        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));

        out.println("<div class='main'>");
        out.println("<div class='page-header'>");
        out.println("<h1>🧾 Bill — " + reservation.getReservationNumber() + "</h1>");
        out.println("<p>Generated on: " + now + "</p>");
        out.println("</div>");

        // Use existing bill values if already generated
        if (existingBill != null) {
            rooms = existingBill.getRoomCharges();
            tax   = existingBill.getTaxAmount();
            total = existingBill.getTotalAmount();
        }

        // ---- Printable Bill Receipt ----
        out.println("<div class='bill-receipt' id='billReceipt'>");

        // Hotel header
        out.println("<div class='receipt-header'>");
        out.println("<h2>🏨 Ocean View Resort</h2>");
        out.println("<p>Galle, Sri Lanka | Tel: +94 91 234 5678</p>");
        out.println("<p>info@oceanviewresort.lk</p>");
        out.println("<div class='receipt-divider'></div>");
        out.println("<h3>GUEST INVOICE</h3>");
        out.println("</div>");

        // Bill meta info
        out.println("<div class='receipt-meta'>");
        out.println("<div><span>Reservation No:</span><strong>" + reservation.getReservationNumber() + "</strong></div>");
        out.println("<div><span>Bill Date:</span><strong>" + now + "</strong></div>");
        out.println("<div><span>Payment Status:</span><strong>" +
                    (existingBill != null ?
                        "<span class='badge " + getBadgeClass(existingBill.getPaymentStatus()) + "'>" + existingBill.getPaymentStatus() + "</span>"
                        : "<span class='badge badge-pending'>PENDING</span>") +
                    "</strong></div>");
        out.println("</div>");

        out.println("<div class='receipt-divider'></div>");

        // Guest & Room info
        out.println("<div class='receipt-info-grid'>");
        out.println("<div><h4>Guest Details</h4>");
        out.println("<p>" + reservation.getGuestName() + "</p></div>");
        out.println("<div><h4>Room Details</h4>");
        out.println("<p>Room " + reservation.getRoomNumber() + " — " + reservation.getRoomTypeName() + "</p></div>");
        out.println("<div><h4>Check-in</h4><p>" + reservation.getCheckInDate() + "</p></div>");
        out.println("<div><h4>Check-out</h4><p>" + reservation.getCheckOutDate() + "</p></div>");
        out.println("</div>");

        out.println("<div class='receipt-divider'></div>");

        // Charges breakdown
        out.println("<table class='charges-table'>");
        out.println("<thead><tr><th>Description</th><th>Qty</th><th>Rate (LKR)</th><th>Amount (LKR)</th></tr></thead>");
        out.println("<tbody>");
        out.println("<tr><td>Room Charges (" + reservation.getRoomTypeName() + ")</td>");
        out.println("<td>" + nights + " night(s)</td>");
        out.println("<td>" + String.format("%,.2f", rate) + "</td>");
        out.println("<td>" + String.format("%,.2f", rooms) + "</td></tr>");
        if (existingBill != null && existingBill.getDiscountAmount() > 0) {
            out.println("<tr class='discount-row'><td>Discount</td><td>-</td><td>-</td><td>- " + String.format("%,.2f", existingBill.getDiscountAmount()) + "</td></tr>");
        }
        out.println("<tr><td>VAT (10%)</td><td>-</td><td>-</td><td>" + String.format("%,.2f", tax) + "</td></tr>");
        out.println("</tbody>");
        out.println("<tfoot><tr class='total-row'><td colspan='3'>TOTAL AMOUNT</td><td>LKR " + String.format("%,.2f", total) + "</td></tr></tfoot>");
        out.println("</table>");

        // Payment method
        if (existingBill != null) {
            out.println("<div class='receipt-divider'></div>");
            out.println("<p style='font-size:13px;color:#555;text-align:center;'>Payment Method: <strong>" + existingBill.getPaymentMethod() + "</strong></p>");
        }

        out.println("<div class='receipt-footer'>");
        out.println("<p>Thank you for staying at Ocean View Resort!</p>");
        out.println("<p style='font-size:11px;color:#aaa;margin-top:6px;'>This is a computer-generated invoice.</p>");
        out.println("</div>");
        out.println("</div>"); // end bill-receipt

        // ---- Action Buttons ----
        out.println("<div class='action-buttons' id='actionButtons'>");

        if (existingBill == null) {
            // Show generate bill form
            out.println("<div class='card' style='margin-top:24px;max-width:500px;'>");
            out.println("<h2>⚙ Generate Bill</h2>");
            out.println("<form method='POST' action='" + contextPath + "/bill'>");
            out.println("<input type='hidden' name='action'          value='generate'>");
            out.println("<input type='hidden' name='reservationId'   value='" + reservationId + "'>");
            out.println("<input type='hidden' name='reservationNumber' value='" + reservation.getReservationNumber() + "'>");
            out.println("<input type='hidden' name='numNights'       value='" + nights + "'>");
            out.println("<input type='hidden' name='ratePerNight'    value='" + rate + "'>");

            out.println("<div class='form-group'>");
            out.println("<label>Discount Amount (LKR)</label>");
            out.println("<input type='number' name='discountAmount' min='0' value='0' " +
                        "style='padding:11px 14px;border:1.5px solid #e0e0e0;border-radius:10px;font-size:14px;width:100%;outline:none;margin-top:6px;' />");
            out.println("</div>");

            out.println("<div class='form-group' style='margin-top:16px;'>");
            out.println("<label>Payment Method</label>");
            out.println("<select name='paymentMethod' style='padding:11px 14px;border:1.5px solid #e0e0e0;border-radius:10px;font-size:14px;width:100%;outline:none;margin-top:6px;'>");
            out.println("<option value='CASH'>💵 Cash</option>");
            out.println("<option value='CARD'>💳 Card</option>");
            out.println("<option value='ONLINE'>🌐 Online</option>");
            out.println("</select></div>");

            out.println("<button type='submit' class='btn btn-primary' style='margin-top:20px;'>✅ Confirm & Save Bill</button>");
            out.println("</form></div>");

        } else {
            // Bill already exists - show print and mark paid buttons
            out.println("<div style='margin-top:20px;display:flex;gap:12px;flex-wrap:wrap;'>");

            // Print button
            out.println("<button onclick='printBill()' class='btn btn-primary'>🖨 Print Bill</button>");

            // Mark as paid (only if not already paid)
            if (!"PAID".equals(existingBill.getPaymentStatus())) {
                out.println("<form method='POST' action='" + contextPath + "/bill' style='display:inline;'>");
                out.println("<input type='hidden' name='action'            value='markPaid'>");
                out.println("<input type='hidden' name='billId'            value='" + existingBill.getBillId() + "'>");
                out.println("<input type='hidden' name='reservationNumber' value='" + reservation.getReservationNumber() + "'>");
                out.println("<button type='submit' class='btn btn-success'>💰 Mark as Paid</button>");
                out.println("</form>");
            }

            out.println("<a href='" + contextPath + "/reservation?action=list' class='btn btn-secondary'>← Back to Reservations</a>");
            out.println("</div>");
        }

        out.println("</div>"); // end action-buttons
        out.println("</div>"); // end main

        // Print script
        out.println("<script>");
        out.println("function printBill() {");
        out.println("  const receipt  = document.getElementById('billReceipt').innerHTML;");
        out.println("  const original = document.body.innerHTML;");
        out.println("  document.body.innerHTML = '<div style=\"max-width:700px;margin:0 auto;padding:40px;font-family:serif;\">' + receipt + '</div>';");
        out.println("  window.print();");
        out.println("  document.body.innerHTML = original;");
        out.println("  location.reload();");
        out.println("}");
        out.println("</script>");

        printPageFooter(out);
    }

    // ------------------------------------------------
    // Shared page header (sidebar + styles)
    // ------------------------------------------------
    private void printPageHeader(PrintWriter out, String title, String contextPath) {
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
        out.println(".page-header p { color:#888; font-size:13px; margin-top:4px; }");

        // Card
        out.println(".card { background:white; border-radius:14px; padding:28px; box-shadow:0 2px 12px rgba(0,0,0,0.07); margin-bottom:24px; }");
        out.println(".card h2 { font-size:16px; font-weight:700; color:#1a1a2e; margin-bottom:20px; padding-bottom:12px; border-bottom:2px solid #f0f4f8; }");
        out.println(".form-group { display:flex; flex-direction:column; gap:6px; margin-bottom:16px; }");
        out.println(".form-group label { font-size:13px; font-weight:600; color:#444; }");

        // Buttons
        out.println(".btn { padding:11px 24px; border:none; border-radius:10px; font-size:14px; font-weight:600; cursor:pointer; text-decoration:none; display:inline-block; transition:transform 0.2s,box-shadow 0.2s; }");
        out.println(".btn-primary  { background:linear-gradient(135deg,#0f4c75,#1b6ca8); color:white; }");
        out.println(".btn-success  { background:linear-gradient(135deg,#065f46,#16a085); color:white; }");
        out.println(".btn-secondary{ background:#f0f4f8; color:#555; }");
        out.println(".btn:hover    { transform:translateY(-2px); box-shadow:0 6px 16px rgba(0,0,0,0.15); }");

        // Badges
        out.println(".badge { padding:4px 12px; border-radius:20px; font-size:11px; font-weight:700; }");
        out.println(".badge-confirmed   { background:#dbeafe; color:#1e40af; }");
        out.println(".badge-paid        { background:#d1fae5; color:#065f46; }");
        out.println(".badge-pending     { background:#fef3c7; color:#92400e; }");
        out.println(".badge-partiallypaid { background:#e0e7ff; color:#3730a3; }");
        out.println(".badge-cancelled   { background:#fee2e2; color:#991b1b; }");

        // Alert
        out.println(".alert { padding:12px 18px; border-radius:10px; font-size:13px; margin-bottom:20px; }");
        out.println(".alert-error   { background:#fef2f2; color:#dc2626; border:1px solid #fecaca; }");
        out.println(".alert-success { background:#f0fdf4; color:#16a34a; border:1px solid #bbf7d0; }");

        // Bill Receipt styles
        out.println(".bill-receipt { background:white; border-radius:14px; padding:40px; max-width:720px; box-shadow:0 2px 12px rgba(0,0,0,0.07); margin-bottom:10px; }");
        out.println(".receipt-header { text-align:center; margin-bottom:20px; }");
        out.println(".receipt-header h2 { font-size:22px; color:#0f4c75; margin-bottom:6px; }");
        out.println(".receipt-header p  { font-size:13px; color:#666; }");
        out.println(".receipt-header h3 { font-size:15px; color:#333; margin-top:12px; letter-spacing:2px; }");
        out.println(".receipt-divider { border:none; border-top:1.5px dashed #e0e0e0; margin:16px 0; }");
        out.println(".receipt-meta { display:grid; grid-template-columns:1fr 1fr 1fr; gap:12px; font-size:13px; }");
        out.println(".receipt-meta div span { color:#888; display:block; font-size:11px; text-transform:uppercase; margin-bottom:3px; }");
        out.println(".receipt-info-grid { display:grid; grid-template-columns:1fr 1fr; gap:16px; font-size:13px; }");
        out.println(".receipt-info-grid h4 { font-size:11px; text-transform:uppercase; color:#888; margin-bottom:4px; }");
        out.println(".receipt-info-grid p  { color:#1a1a2e; font-weight:500; }");
        out.println(".charges-table { width:100%; border-collapse:collapse; font-size:13px; margin-top:8px; }");
        out.println(".charges-table th { background:#f8fafc; padding:10px 14px; text-align:left; font-size:12px; color:#666; text-transform:uppercase; border-bottom:2px solid #f0f4f8; }");
        out.println(".charges-table td { padding:12px 14px; border-bottom:1px solid #f8fafc; color:#333; }");
        out.println(".charges-table th:last-child, .charges-table td:last-child { text-align:right; }");
        out.println(".total-row td { font-weight:800; font-size:15px; color:#0f4c75; background:#f0f7ff; border-top:2px solid #bee3f8; }");
        out.println(".discount-row td { color:#16a085; }");
        out.println(".receipt-footer { text-align:center; margin-top:24px; font-size:13px; color:#666; padding-top:16px; border-top:1.5px dashed #e0e0e0; }");

        // Print styles
        out.println("@media print {");
        out.println("  .sidebar, .action-buttons, .page-header { display:none !important; }");
        out.println("  .main { margin-left:0 !important; padding:0 !important; }");
        out.println("  .bill-receipt { box-shadow:none; padding:20px; max-width:100%; }");
        out.println("}");
        out.println("</style></head><body>");

        // Sidebar
        out.println("<div class='sidebar'>");
        out.println("<div class='logo'><h2>🏨 Ocean View</h2></div><nav>");
        out.println("<a href='" + contextPath + "/dashboard'>🏠 Dashboard</a>");
        out.println("<a href='" + contextPath + "/reservation?action=add'>➕ New Reservation</a>");
        out.println("<a href='" + contextPath + "/reservation?action=list'>📋 All Reservations</a>");
        out.println("<a href='" + contextPath + "/room?action=list'>🛏 Rooms</a>");
        out.println("<a href='" + contextPath + "/bill' class='active'>💰 Billing</a>");
        if ("ADMIN".equals(role)) {
            out.println("<a href='" + contextPath + "/user?action=list'>👥 Manage Users</a>");
        }
        out.println("</nav>");
        out.println("<div class='logout'><a href='" + contextPath + "/logout'>🚪 Logout</a></div>");
        out.println("</div>");
    }

    // ------------------------------------------------
    // Shared page footer
    // ------------------------------------------------
    private void printPageFooter(PrintWriter out) {
        out.println("</body></html>");
    }

    // ------------------------------------------------
    // Helper - get badge CSS class from status string
    // ------------------------------------------------
    private String getBadgeClass(String status) {
        if (status == null) return "";
        switch (status.toUpperCase()) {
            case "CONFIRMED":      return "badge-confirmed";
            case "PAID":           return "badge-paid";
            case "PENDING":        return "badge-pending";
            case "PARTIALLY_PAID": return "badge-partiallypaid";
            case "CANCELLED":      return "badge-cancelled";
            case "CHECKED_IN":     return "badge-checkedin";
            case "CHECKED_OUT":    return "badge-checkedout";
            default:               return "";
        }
    }
}