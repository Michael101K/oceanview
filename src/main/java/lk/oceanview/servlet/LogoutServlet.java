package lk.oceanview.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * LogoutServlet
 * Invalidates the session and redirects back to login page.
 */
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session != null) {
            String username = (String) session.getAttribute("username");
            session.invalidate();
            System.out.println("[LogoutServlet] User logged out: " + username);
        }

        response.sendRedirect(request.getContextPath() + "/login.html?message=loggedout");
    }
}