package it.polimi.tiw.projects.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import it.polimi.tiw.projects.beans.User;
import it.polimi.tiw.projects.dao.PlaylistDAO;
import it.polimi.tiw.projects.utils.ConnectionHandler;

@WebServlet("/AddSongsToPlaylist")
public class AddSongsToPlaylist extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection;

    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        if (session.isNew() || user == null) {
            response.sendRedirect(getServletContext().getContextPath() + "/index.html");
            return;
        }
        
		JakartaServletWebApplication webApplication = JakartaServletWebApplication.buildApplication(getServletContext());
        WebContext ctx = new WebContext(webApplication.buildExchange(request, response), request.getLocale());
		
        String ctxpath = getServletContext().getContextPath();
		String homePath = ctxpath + "/Home";
        String playlistPath = ctxpath + "/Playlist?id=";
		
        String[] songIds = request.getParameterValues("songIds");
        String playlistIdParam = request.getParameter("playlistId");

        int playlistId;
        
        try {
            playlistId = Integer.parseInt(playlistIdParam);
        } catch (Exception e) {
            //response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid playlist ID");
            response.sendRedirect(homePath);
        	return;
        }
        
        if (songIds == null) {
            //response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing playlist or songs");
        	request.getSession().setAttribute("songErrorMsg", "No songs selected");
        	response.sendRedirect(playlistPath + playlistId);
        	return;
        }

        
        try {
            PlaylistDAO playlistDAO = new PlaylistDAO(connection);

            for (String songIdStr : songIds) {
                int songId = Integer.parseInt(songIdStr);
                playlistDAO.addSongToPlaylist(playlistId, songId);
            }
        } catch (SQLException e) {
            //e.printStackTrace();
            //response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
        	request.getSession().setAttribute("songErrorMsg", "Unable to complete database operation");
        	response.sendRedirect(playlistPath + playlistId);
        	return;
        }

        // Redirect alla pagina playlist aggiornata
        response.sendRedirect(getServletContext().getContextPath() + "/Playlist?id=" + playlistId);
    }

    public void destroy() {
        try {
            ConnectionHandler.closeConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
