package lk.oceanview.servlet;

import lk.oceanview.dao.ServiceDAO;
import lk.oceanview.model.Service;
import lk.oceanview.model.User;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * ServiceServlet
 * GET  ?action=list    -> show all services
 * POST action=add      -> add new service
 * POST action=update   -> update existing service
 * POST action=delete   -> delete a service
 * POST action=toggle   -> toggle availability
 */
public class ServiceServlet extends HttpServlet {

    private ServiceDAO serviceDAO;

    // Service categories with icons
    private static final Map<String, String> CATEGORIES = new LinkedHashMap<>();
    static {
        CATEGORIES.put("Dining",       "🍽");
        CATEGORIES.put("Spa & Wellness","💆");
        CATEGORIES.put("Recreation",   "🏄");
        CATEGORIES.put("Transport",    "🚗");
        CATEGORIES.put("Room Service", "🛎");
        CATEGORIES.put("Other",        "⭐");
    }

    @Override
    public void init() throws ServletException {
        serviceDAO = new ServiceDAO();
    }

    // ------------------------------------------------
    // Session check helper
    // ------------------------------------------------
    private boolean isLoggedIn(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedInUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login.html?error=session");
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
        if (!isLoggedIn(request, response)) return;
        showServiceList(request, response);
    }

    // ------------------------------------------------
    // POST
    // ------------------------------------------------
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isLoggedIn(request, response)) return;

        // Only admin can modify services
        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute("loggedInUser");
        String contextPath = request.getContextPath();

        if (!user.isAdmin()) {
            response.sendRedirect(contextPath + "/service?action=list&error=unauthorized");
            return;
        }

        String action = request.getParameter("action");
        switch (action != null ? action : "") {
            case "add":    addService(request, response, contextPath);    break;
            case "update": updateService(request, response, contextPath); break;
            case "delete": deleteService(request, response, contextPath); break;
            case "toggle": toggleService(request, response, contextPath); break;
            default: response.sendRedirect(contextPath + "/service?action=list");
        }
    }

    // ------------------------------------------------
    // Show Service List
    // ------------------------------------------------
    private void showServiceList(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        List<Service> services = serviceDAO.getAllServices();
        HttpSession session    = request.getSession(false);
        User user              = (User) session.getAttribute("loggedInUser");
        String contextPath     = request.getContextPath();
        String msg             = request.getParameter("msg");
        String error           = request.getParameter("error");

        // Count stats
        long available   = services.stream().filter(Service::isAvailable).count();
        long unavailable = services.size() - available;

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        printHeader(out, "Services | Ocean View Resort", contextPath, user);

        out.println("<div class='main'>");
        out.println("<div class='page-header'>");
        out.println("<div style='display:flex;justify-content:space-between;align-items:center;'>");
        out.println("<div><h1>⭐ Resort Services</h1><p>Manage all services offered at Ocean View Resort</p></div>");
        if (user.isAdmin()) {
            out.println("<button onclick='openAddModal()' class='btn btn-primary'>➕ Add New Service</button>");
        }
        out.println("</div></div>");

        // Alerts
        if ("added".equals(msg))    out.println("<div class='alert alert-success'>✅ Service added successfully.</div>");
        if ("updated".equals(msg))  out.println("<div class='alert alert-success'>✅ Service updated successfully.</div>");
        if ("deleted".equals(msg))  out.println("<div class='alert alert-success'>✅ Service deleted successfully.</div>");
        if ("toggled".equals(msg))  out.println("<div class='alert alert-success'>✅ Service availability updated.</div>");
        if ("unauthorized".equals(error)) out.println("<div class='alert alert-error'>⚠ Only administrators can modify services.</div>");

        // Stats
        out.println("<div class='stats-grid'>");
        out.println("<div class='stat-card blue'><div><div class='number'>" + services.size() + "</div><div class='label'>Total Services</div></div><div class='icon'>⭐</div></div>");
        out.println("<div class='stat-card green'><div><div class='number'>" + available + "</div><div class='label'>Available</div></div><div class='icon'>✅</div></div>");
        out.println("<div class='stat-card red'><div><div class='number'>" + unavailable + "</div><div class='label'>Unavailable</div></div><div class='icon'>❌</div></div>");
        out.println("<div class='stat-card purple'><div><div class='number'>" + CATEGORIES.size() + "</div><div class='label'>Categories</div></div><div class='icon'>📂</div></div>");
        out.println("</div>");

        // Services by category
        for (Map.Entry<String, String> cat : CATEGORIES.entrySet()) {
            String catName = cat.getKey();
            String catIcon = cat.getValue();

            List<Service> catServices = services.stream()
                .filter(s -> catName.equals(s.getCategory()))
                .collect(java.util.stream.Collectors.toList());

            if (catServices.isEmpty()) continue;

            out.println("<div class='card'>");
            out.println("<h2>" + catIcon + " " + catName + " <span style='font-size:13px;color:#aaa;font-weight:400;'>(" + catServices.size() + " services)</span></h2>");
            out.println("<div class='services-grid'>");

            for (Service s : catServices) {
                String cardClass = s.isAvailable() ? "service-card available" : "service-card unavailable";
                out.println("<div class='" + cardClass + "'>");
                out.println("<div class='service-header'>");
                out.println("<div class='service-name'>" + s.getServiceName() + "</div>");
                out.println("<span class='badge " + (s.isAvailable() ? "badge-available" : "badge-unavailable") + "'>" +
                            (s.isAvailable() ? "Available" : "Unavailable") + "</span>");
                out.println("</div>");
                out.println("<div class='service-desc'>" + (s.getDescription() != null ? s.getDescription() : "") + "</div>");
                out.println("<div class='service-price'>LKR " + String.format("%,.2f", s.getPrice()) + "</div>");

                if (user.isAdmin()) {
                    out.println("<div class='service-actions'>");

                    // Edit button
                    out.println("<button onclick='openEditModal(" +
                                s.getServiceId() + ",\"" + escape(s.getServiceName()) + "\",\"" +
                                escape(s.getDescription() != null ? s.getDescription() : "") + "\"," +
                                s.getPrice() + ",\"" + s.getCategory() + "\"," +
                                s.isAvailable() + ")' class='btn-sm btn-edit'>✏ Edit</button>");

                    // Toggle availability
                    out.println("<form method='POST' action='" + contextPath + "/service' style='display:inline;'>");
                    out.println("<input type='hidden' name='action'    value='toggle'>");
                    out.println("<input type='hidden' name='serviceId' value='" + s.getServiceId() + "'>");
                    out.println("<input type='hidden' name='isAvailable' value='" + !s.isAvailable() + "'>");
                    String toggleLabel = s.isAvailable() ? "🔒 Disable" : "🔓 Enable";
                    String toggleClass = s.isAvailable() ? "btn-sm btn-warning" : "btn-sm btn-success";
                    out.println("<button type='submit' class='" + toggleClass + "'>" + toggleLabel + "</button>");
                    out.println("</form>");

                    // Delete button
                    out.println("<form method='POST' action='" + contextPath + "/service' style='display:inline;' " +
                                "onsubmit='return confirm(\"Are you sure you want to delete this service?\")'>");
                    out.println("<input type='hidden' name='action'    value='delete'>");
                    out.println("<input type='hidden' name='serviceId' value='" + s.getServiceId() + "'>");
                    out.println("<button type='submit' class='btn-sm btn-danger'>🗑 Delete</button>");
                    out.println("</form>");

                    out.println("</div>"); // end service-actions
                }
                out.println("</div>"); // end service-card
            }
            out.println("</div></div>"); // end services-grid + card
        }

        if (services.isEmpty()) {
            out.println("<div class='card'><p style='text-align:center;color:#aaa;padding:40px;'>No services added yet. Click \"Add New Service\" to get started.</p></div>");
        }

        // ---- Add Modal ----
        if (user.isAdmin()) {
            out.println("<div id='addModal' class='modal' style='display:none;'>");
            out.println("<div class='modal-content'>");
            out.println("<div class='modal-header'><h3>➕ Add New Service</h3><span onclick='closeAddModal()' class='close-btn'>✕</span></div>");
            out.println("<form method='POST' action='" + contextPath + "/service'>");
            out.println("<input type='hidden' name='action' value='add'>");
            out.println("<div class='form-grid'>");
            out.println("<div class='form-group'><label>Service Name *</label><input type='text' name='serviceName' placeholder='e.g. Spa Treatment' required /></div>");
            out.println("<div class='form-group'><label>Category *</label><select name='category'>");
            for (Map.Entry<String, String> cat : CATEGORIES.entrySet()) {
                out.println("<option value='" + cat.getKey() + "'>" + cat.getValue() + " " + cat.getKey() + "</option>");
            }
            out.println("</select></div>");
            out.println("<div class='form-group'><label>Price (LKR) *</label><input type='number' name='price' min='0' step='0.01' placeholder='e.g. 2500.00' required /></div>");
            out.println("<div class='form-group'><label>Availability</label><select name='isAvailable'><option value='true'>✅ Available</option><option value='false'>❌ Unavailable</option></select></div>");
            out.println("<div class='form-group' style='grid-column:1/-1;'><label>Description</label><textarea name='description' rows='2' placeholder='Brief description of the service...' style='padding:11px 14px;border:1.5px solid #e0e0e0;border-radius:10px;font-size:14px;width:100%;outline:none;font-family:inherit;resize:vertical;'></textarea></div>");
            out.println("</div>");
            out.println("<div style='margin-top:20px;display:flex;gap:10px;'>");
            out.println("<button type='submit' class='btn btn-primary'>✅ Add Service</button>");
            out.println("<button type='button' onclick='closeAddModal()' class='btn btn-secondary'>Cancel</button>");
            out.println("</div></form></div></div>");

            // ---- Edit Modal ----
            out.println("<div id='editModal' class='modal' style='display:none;'>");
            out.println("<div class='modal-content'>");
            out.println("<div class='modal-header'><h3>✏ Edit Service</h3><span onclick='closeEditModal()' class='close-btn'>✕</span></div>");
            out.println("<form method='POST' action='" + contextPath + "/service'>");
            out.println("<input type='hidden' name='action' value='update'>");
            out.println("<input type='hidden' name='serviceId' id='editServiceId'>");
            out.println("<div class='form-grid'>");
            out.println("<div class='form-group'><label>Service Name *</label><input type='text' name='serviceName' id='editName' required /></div>");
            out.println("<div class='form-group'><label>Category *</label><select name='category' id='editCategory'>");
            for (Map.Entry<String, String> cat : CATEGORIES.entrySet()) {
                out.println("<option value='" + cat.getKey() + "'>" + cat.getValue() + " " + cat.getKey() + "</option>");
            }
            out.println("</select></div>");
            out.println("<div class='form-group'><label>Price (LKR) *</label><input type='number' name='price' id='editPrice' min='0' step='0.01' required /></div>");
            out.println("<div class='form-group'><label>Availability</label><select name='isAvailable' id='editAvailable'><option value='true'>✅ Available</option><option value='false'>❌ Unavailable</option></select></div>");
            out.println("<div class='form-group' style='grid-column:1/-1;'><label>Description</label><textarea name='description' id='editDesc' rows='2' style='padding:11px 14px;border:1.5px solid #e0e0e0;border-radius:10px;font-size:14px;width:100%;outline:none;font-family:inherit;resize:vertical;'></textarea></div>");
            out.println("</div>");
            out.println("<div style='margin-top:20px;display:flex;gap:10px;'>");
            out.println("<button type='submit' class='btn btn-primary'>💾 Save Changes</button>");
            out.println("<button type='button' onclick='closeEditModal()' class='btn btn-secondary'>Cancel</button>");
            out.println("</div></form></div></div>");
        }

        out.println("</div>"); // end main

        // JavaScript
        out.println("<script>");
        out.println("function openAddModal()  { document.getElementById('addModal').style.display='flex'; }");
        out.println("function closeAddModal() { document.getElementById('addModal').style.display='none'; }");
        out.println("function closeEditModal(){ document.getElementById('editModal').style.display='none'; }");
        out.println("function openEditModal(id, name, desc, price, category, available) {");
        out.println("  document.getElementById('editServiceId').value = id;");
        out.println("  document.getElementById('editName').value      = name;");
        out.println("  document.getElementById('editDesc').value      = desc;");
        out.println("  document.getElementById('editPrice').value     = price;");
        out.println("  document.getElementById('editCategory').value  = category;");
        out.println("  document.getElementById('editAvailable').value = available.toString();");
        out.println("  document.getElementById('editModal').style.display = 'flex';");
        out.println("}");
        out.println("window.onclick = function(e) {");
        out.println("  if (e.target.id==='addModal')  closeAddModal();");
        out.println("  if (e.target.id==='editModal') closeEditModal();");
        out.println("}");
        out.println("</script>");

        printFooter(out);
    }

    // ------------------------------------------------
    // Add service
    // ------------------------------------------------
    private void addService(HttpServletRequest request, HttpServletResponse response,
                            String contextPath) throws IOException {
        Service service = new Service();
        service.setServiceName(request.getParameter("serviceName"));
        service.setDescription(request.getParameter("description"));
        service.setPrice(Double.parseDouble(request.getParameter("price")));
        service.setCategory(request.getParameter("category"));
        service.setAvailable(Boolean.parseBoolean(request.getParameter("isAvailable")));

        boolean success = serviceDAO.addService(service);
        response.sendRedirect(contextPath + "/service?action=list&msg=" + (success ? "added" : "error"));
    }

    // ------------------------------------------------
    // Update service
    // ------------------------------------------------
    private void updateService(HttpServletRequest request, HttpServletResponse response,
                               String contextPath) throws IOException {
        try {
            Service service = new Service();
            service.setServiceId(Integer.parseInt(request.getParameter("serviceId")));
            service.setServiceName(request.getParameter("serviceName"));
            service.setDescription(request.getParameter("description"));
            service.setPrice(Double.parseDouble(request.getParameter("price")));
            service.setCategory(request.getParameter("category"));
            service.setAvailable(Boolean.parseBoolean(request.getParameter("isAvailable")));

            serviceDAO.updateService(service);
            response.sendRedirect(contextPath + "/service?action=list&msg=updated");
        } catch (Exception e) {
            System.err.println("[ServiceServlet] updateService error: " + e.getMessage());
            response.sendRedirect(contextPath + "/service?action=list");
        }
    }

    // ------------------------------------------------
    // Delete service
    // ------------------------------------------------
    private void deleteService(HttpServletRequest request, HttpServletResponse response,
                               String contextPath) throws IOException {
        try {
            int serviceId = Integer.parseInt(request.getParameter("serviceId"));
            serviceDAO.deleteService(serviceId);
            response.sendRedirect(contextPath + "/service?action=list&msg=deleted");
        } catch (Exception e) {
            System.err.println("[ServiceServlet] deleteService error: " + e.getMessage());
            response.sendRedirect(contextPath + "/service?action=list");
        }
    }

    // ------------------------------------------------
    // Toggle availability
    // ------------------------------------------------
    private void toggleService(HttpServletRequest request, HttpServletResponse response,
                               String contextPath) throws IOException {
        try {
            int     serviceId   = Integer.parseInt(request.getParameter("serviceId"));
            boolean isAvailable = Boolean.parseBoolean(request.getParameter("isAvailable"));
            Service service     = serviceDAO.getServiceById(serviceId);
            if (service != null) {
                service.setAvailable(isAvailable);
                serviceDAO.updateService(service);
            }
            response.sendRedirect(contextPath + "/service?action=list&msg=toggled");
        } catch (Exception e) {
            System.err.println("[ServiceServlet] toggleService error: " + e.getMessage());
            response.sendRedirect(contextPath + "/service?action=list");
        }
    }

    // ------------------------------------------------
    // Escape special characters for JS strings
    // ------------------------------------------------
    private String escape(String s) {
        return s.replace("\"", "&quot;").replace("'", "&#39;");
    }

    // ------------------------------------------------
    // Shared page header
    // ------------------------------------------------
    private void printHeader(PrintWriter out, String title, String contextPath, User user) {
        out.println("<!DOCTYPE html><html lang='en'><head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("<title>" + title + "</title>");
        out.println("<style>");
        out.println("* { margin:0; padding:0; box-sizing:border-box; }");
        out.println("body { font-family:'Segoe UI',sans-serif; background:#f0f4f8; }");

        // Sidebar
        out.println(".sidebar { width:240px; height:100vh; background:linear-gradient(180deg,#0f4c75,#1b6ca8); position:fixed; top:0; left:0; padding:30px 0; color:white; overflow-y:auto; }");
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
        out.println(".stat-card.blue   { border-color:#1b6ca8; } .stat-card.green { border-color:#16a085; }");
        out.println(".stat-card.red    { border-color:#e74c3c; } .stat-card.purple{ border-color:#8e44ad; }");
        out.println(".stat-card .number { font-size:30px; font-weight:800; color:#1a1a2e; }");
        out.println(".stat-card .label  { font-size:12px; color:#888; margin-top:4px; }");
        out.println(".stat-card .icon   { font-size:28px; }");

        // Card
        out.println(".card { background:white; border-radius:14px; padding:24px; box-shadow:0 2px 12px rgba(0,0,0,0.07); margin-bottom:24px; }");
        out.println(".card h2 { font-size:16px; font-weight:700; color:#1a1a2e; margin-bottom:16px; padding-bottom:12px; border-bottom:2px solid #f0f4f8; }");

        // Services grid
        out.println(".services-grid { display:grid; grid-template-columns:repeat(auto-fill,minmax(260px,1fr)); gap:16px; }");
        out.println(".service-card { border-radius:12px; padding:18px; border:1.5px solid; transition:transform 0.2s; }");
        out.println(".service-card:hover { transform:translateY(-3px); }");
        out.println(".service-card.available   { border-color:#bbf7d0; background:#f0fdf4; }");
        out.println(".service-card.unavailable { border-color:#fecaca; background:#fef2f2; }");
        out.println(".service-header { display:flex; justify-content:space-between; align-items:flex-start; margin-bottom:8px; }");
        out.println(".service-name { font-size:15px; font-weight:700; color:#1a1a2e; }");
        out.println(".service-desc { font-size:12px; color:#888; margin-bottom:10px; line-height:1.5; }");
        out.println(".service-price { font-size:15px; font-weight:700; color:#1b6ca8; margin-bottom:12px; }");
        out.println(".service-actions { display:flex; gap:6px; flex-wrap:wrap; }");

        // Badges
        out.println(".badge { padding:4px 10px; border-radius:20px; font-size:11px; font-weight:700; }");
        out.println(".badge-available   { background:#d1fae5; color:#065f46; }");
        out.println(".badge-unavailable { background:#fee2e2; color:#991b1b; }");

        // Buttons
        out.println(".btn { padding:11px 22px; border:none; border-radius:10px; font-size:13px; font-weight:600; cursor:pointer; text-decoration:none; display:inline-block; transition:transform 0.2s; }");
        out.println(".btn-primary   { background:linear-gradient(135deg,#0f4c75,#1b6ca8); color:white; }");
        out.println(".btn-secondary { background:#f0f4f8; color:#555; }");
        out.println(".btn:hover { transform:translateY(-2px); }");
        out.println(".btn-sm { padding:6px 12px; border:none; border-radius:7px; font-size:11px; font-weight:600; cursor:pointer; }");
        out.println(".btn-edit    { background:#ede9fe; color:#5b21b6; }");
        out.println(".btn-danger  { background:#fee2e2; color:#991b1b; }");
        out.println(".btn-warning { background:#fef3c7; color:#92400e; }");
        out.println(".btn-success { background:#d1fae5; color:#065f46; }");

        // Form
        out.println(".form-grid { display:grid; grid-template-columns:1fr 1fr; gap:16px; }");
        out.println(".form-group { display:flex; flex-direction:column; gap:6px; }");
        out.println(".form-group label { font-size:13px; font-weight:600; color:#444; }");
        out.println(".form-group input, .form-group select { padding:11px 14px; border:1.5px solid #e0e0e0; border-radius:10px; font-size:14px; color:#333; outline:none; font-family:inherit; }");
        out.println(".form-group input:focus, .form-group select:focus { border-color:#1b6ca8; }");

        // Alert
        out.println(".alert { padding:12px 18px; border-radius:10px; font-size:13px; margin-bottom:20px; }");
        out.println(".alert-success { background:#f0fdf4; color:#16a34a; border:1px solid #bbf7d0; }");
        out.println(".alert-error   { background:#fef2f2; color:#dc2626; border:1px solid #fecaca; }");

        // Modal
        out.println(".modal { position:fixed; top:0; left:0; width:100%; height:100%; background:rgba(0,0,0,0.5); display:flex; align-items:center; justify-content:center; z-index:1000; }");
        out.println(".modal-content { background:white; border-radius:16px; padding:32px; width:560px; max-width:90%; max-height:90vh; overflow-y:auto; box-shadow:0 20px 60px rgba(0,0,0,0.3); }");
        out.println(".modal-header { display:flex; justify-content:space-between; align-items:center; margin-bottom:24px; padding-bottom:14px; border-bottom:2px solid #f0f4f8; }");
        out.println(".modal-header h3 { font-size:17px; font-weight:700; color:#1a1a2e; }");
        out.println(".close-btn { cursor:pointer; font-size:20px; color:#888; }");
        out.println(".close-btn:hover { color:#333; }");

        out.println("</style></head><body>");

        // Sidebar
        out.println("<div class='sidebar'>");
        out.println("<div class='logo'><h2>🏨 Ocean View</h2></div><nav>");
        out.println("<a href='" + contextPath + "/dashboard'>🏠 Dashboard</a>");
        out.println("<a href='" + contextPath + "/reservation?action=add'>➕ New Reservation</a>");
        out.println("<a href='" + contextPath + "/reservation?action=list'>📋 All Reservations</a>");
        out.println("<a href='" + contextPath + "/guest?action=history'>👤 Guest History</a>");
        out.println("<a href='" + contextPath + "/room?action=list'>🛏 Rooms</a>");
        out.println("<a href='" + contextPath + "/service?action=list' class='active'>⭐ Services</a>");
        out.println("<a href='" + contextPath + "/bill'>💰 Billing</a>");
        out.println("<a href='" + contextPath + "/report'>📊 Reports</a>");
        if (user.isAdmin()) {
            out.println("<a href='" + contextPath + "/user?action=list'>👥 Manage Users</a>");
        }
        out.println("<a href='" + contextPath + "/profile'>👤 My Profile</a>");
        out.println("<a href='" + contextPath + "/help'>❓ Help & Guide</a>");
        out.println("</nav>");
        out.println("<div class='logout'><a href='" + contextPath + "/logout'>🚪 Logout</a></div>");
        out.println("</div>");
    }

    // ------------------------------------------------
    // Shared footer
    // ------------------------------------------------
    private void printFooter(PrintWriter out) {
        out.println("</body></html>");
    }
}