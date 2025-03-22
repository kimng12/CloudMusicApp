package com.amazonaws.cloudmusic.webapp;

import com.amazonaws.cloudmusic.authentication.LoginHandler;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private LoginHandler loginHandler;

    @Override
    public void init() {
        loginHandler = new LoginHandler();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if (loginHandler.authenticate(email, password)) {
            // store username in session for later use
            String username = loginHandler.getUsername(email);
            HttpSession session = request.getSession();
            session.setAttribute("email", email);
            session.setAttribute("username", username);

            // redirect to main page
            response.sendRedirect("main.html");  // update to servlet route if needed
        } else {
            // invalid login, show error
            request.setAttribute("errorMessage", "Email or password is invalid");
            request.getRequestDispatcher("login.html").forward(request, response);
        }
    }
}
