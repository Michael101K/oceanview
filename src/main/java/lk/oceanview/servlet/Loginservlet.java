package lk.oceanview.servlet;

import lk.oceanview.dao.UserDAO;
import lk.oceanview.model.User;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * LoginServlet
 * Handles GET  -> shows the login page
 * Handles POST -> processes login form submission
 */
public class LoginServlet extends HttpServlet {

    private UserDAO userDAO;

    @Override
    public void init() throws ServletException {
        userDAO = new UserDAO();
    }

    // ------------------------------------------------
    // GET - redirect to login.html (already a static file)
    // ------------------------------------------------
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // If user is already logged in, redirect to dashboard
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("loggedInUser") != null) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        // Otherwise show login page
        response.sendRedirect(request.getContextPath() + "/login.html");
    }

    // ------------------------------------------------
    // POST - process login form
    // ------------------------------------------------
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // Basic validation
        if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/login.html?error=empty");
            return;
        }

        // Authenticate against database
        User user = userDAO.authenticate(username.trim(), password.trim());

        if (user != null) {
            // Login successful - create session
            HttpSession session = request.getSession(true);
            session.setAttribute("loggedInUser", user);
            session.setAttribute("userId",   user.getUserId());
            session.setAttribute("username", user.getUsername());
            session.setAttribute("fullName", user.getFullName());
            session.setAttribute("role",     user.getRole());
            session.setMaxInactiveInterval(30 * 60); // 30 minutes

            System.out.println("[LoginServlet] User logged in: " + user.getUsername() + " | Role: " + user.getRole());

            // Redirect to dashboard
            response.sendRedirect(request.getContextPath() + "/dashboard");

        } else {
            // Login failed
            System.out.println("[LoginServlet] Failed login attempt for username: " + username);
            response.sendRedirect(request.getContextPath() + "/login.html?error=invalid");
        }
    }
}