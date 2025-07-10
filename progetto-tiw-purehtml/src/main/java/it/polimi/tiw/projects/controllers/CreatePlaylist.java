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
import java.text.ParseException;

import org.apache.commons.lang.StringEscapeUtils;

import it.polimi.tiw.projects.beans.Playlist;
import it.polimi.tiw.projects.beans.User;
import it.polimi.tiw.projects.dao.PlaylistDAO;
import it.polimi.tiw.projects.utils.ConnectionHandler;

@WebServlet("/CreatePlaylist")
public class CreatePlaylist extends HttpServlet {
	private static final long serialVersionUID = 1L;
    
	private Connection connection = null;
   
    public CreatePlaylist() {
        super();
    }

    public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	HttpSession session = request.getSession();
    	if (session.isNew() || session.getAttribute("user") == null) {
    		String loginpath = getServletContext().getContextPath() + "/index.html";
    		response.sendRedirect(loginpath);
    		return;
    	}

    	boolean isBadRequest = false;
    	String title = null;
    	String[] selectedSongIds = request.getParameterValues("songIds");
    	
    	if (selectedSongIds == null || selectedSongIds.length == 0) {
    		response.sendError(HttpServletResponse.SC_BAD_REQUEST, "You did not select any song");
    		return;
    	}

    	try {
    		title = StringEscapeUtils.escapeJava(request.getParameter("title"));
    		isBadRequest = title == null || title.trim().isEmpty();
    	} catch (Exception e) {
    		isBadRequest = true;
    		e.printStackTrace();
    	}

    	if (isBadRequest) {
    		response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect or missing param values");
    		return;
    	}

    	User user = (User) session.getAttribute("user");
    	PlaylistDAO playlistDAO = new PlaylistDAO(connection);

    	try {
            connection.setAutoCommit(false); 

            java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
            String username = user.getUsername();

            int playlistId = playlistDAO.createPlaylist(title, today, username);

            for (String songIdStr : selectedSongIds) {
                int songId = Integer.parseInt(songIdStr);
                playlistDAO.addSongToPlaylist(playlistId, songId);
            }

            connection.commit();
            connection.setAutoCommit(true); 

        } catch (SQLException | NumberFormatException e) {
            try {
                connection.rollback(); 
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to create playlist");
            return;
        }

    	String ctxpath = getServletContext().getContextPath();
    	String path = ctxpath + "/Home";
    	response.sendRedirect(path);
    }

	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


}
