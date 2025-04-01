package com.amazonaws.cloudmusic.webapp;
import com.amazonaws.cloudmusic.authentication.RegisterHandler;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet{
    private RegisterHandler registerHandler;
    @Override
    public void init() {
        registerHandler = new RegisterHandler();
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String email = request.getParameter("email");
            String password = request.getParameter("password");
            String user_name = request.getParameter("user_name");
            String message = registerHandler.registerUser(email, password,user_name);
            if (message.contains("Succesfully added user")){
                response.sendRedirect("login.html");
            }else{
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);  // 400 Bad Request
                response.setContentType("text/html");
                PrintWriter writer = response.getWriter();
                writer.append("Registration failed: " + message);
            }
        }catch (IOException e){
            response.setStatus(500);
            response.setContentType("text/xml");
            PrintWriter writer=response.getWriter();
            writer.append("Registration failed: "+e.getMessage() );
        }

    }
}