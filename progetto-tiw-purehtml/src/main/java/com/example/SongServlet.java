package com.example;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

@WebServlet("/songs")
public class SongServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        // Leggi i parametri dal web.xml
        ServletContext context = getServletContext();
        String jdbcURL = context.getInitParameter("dbUrl");
        String dbUser = context.getInitParameter("dbUser");
        String dbPassword = context.getInitParameter("dbPassword");
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(jdbcURL, dbUser, dbPassword);

            String sql = "SELECT title FROM song ORDER BY id LIMIT 2";
            PreparedStatement statement = conn.prepareStatement(sql);
            ResultSet rs = statement.executeQuery();

            out.println("<html><body>");
            out.println("<h1>Prime 2 Canzoni</h1>");
            out.println("<ul>");

            while (rs.next()) {
                String titolo = rs.getString("title");
                out.println("<li>" + titolo + "</li>");
            }

            out.println("</ul>");
            out.println("</body></html>");

            rs.close();
            statement.close();
            conn.close();

        } catch (Exception e) {
            e.printStackTrace(out);
        }
    }
}

