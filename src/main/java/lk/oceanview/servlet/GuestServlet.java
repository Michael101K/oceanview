package lk.oceanview.servlet;

import com.google.gson.Gson;
import lk.oceanview.dao.GuestDAO;
import lk.oceanview.model.Guest;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * GuestServlet
 * GET  ?action=lookup&nic=XXXXX  -> returns guest JSON (for auto-fill)
 * POST action=add                -> registers a new guest
 */
public class GuestServlet extends HttpServlet {

    private GuestDAO guestDAO;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        guestDAO = new GuestDAO();
        gson     = new Gson();
    }

    // ------------------------------------------------
    // GET - look up guest by NIC (called via JS fetch)
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
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        if ("lookup".equals(action)) {
            String nic = request.getParameter("nic");
            if (nic != null && !nic.trim().isEmpty()) {
                Guest guest = guestDAO.getGuestByNIC(nic.trim());
                if (guest != null) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("found",         true);
                    result.put("guestId",        guest.getGuestId());
                    result.put("fullName",       guest.getFullName());
                    result.put("address",        guest.getAddress());
                    result.put("contactNumber",  guest.getContactNumber());
                    result.put("email",          guest.getEmail());
                    out.print(gson.toJson(result));
                } else {
                    out.print("{\"found\": false}");
                }
            } else {
                out.print("{\"found\": false}");
            }
        }
    }

    // ------------------------------------------------
    // POST - add a new guest, return guestId as JSON
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

        String fullName       = request.getParameter("fullName");
        String address        = request.getParameter("address");
        String contactNumber  = request.getParameter("contactNumber");
        String email          = request.getParameter("email");
        String nicNumber      = request.getParameter("nicNumber");

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        // Validation
        if (fullName == null || fullName.trim().isEmpty() ||
            address == null || address.trim().isEmpty() ||
            contactNumber == null || contactNumber.trim().isEmpty()) {
            out.print("{\"success\": false, \"message\": \"Required fields are missing.\"}");
            return;
        }

        // Check if guest already exists by NIC
        if (nicNumber != null && !nicNumber.trim().isEmpty()) {
            Guest existing = guestDAO.getGuestByNIC(nicNumber.trim());
            if (existing != null) {
                // Guest already exists - return their ID
                out.print("{\"success\": true, \"guestId\": " + existing.getGuestId() + ", \"existing\": true}");
                return;
            }
        }

        // Create and save new guest
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
}