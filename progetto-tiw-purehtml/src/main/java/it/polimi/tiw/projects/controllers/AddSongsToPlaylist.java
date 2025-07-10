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

        String[] songIds = request.getParameterValues("songIds");
        String playlistIdParam = request.getParameter("playlistId");

        if (songIds == null || playlistIdParam == null || playlistIdParam.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing playlist or songs");
            return;
        }

        int playlistId;
        try {
            playlistId = Integer.parseInt(playlistIdParam);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid playlist ID");
            return;
        }

        try {
            PlaylistDAO playlistDAO = new PlaylistDAO(connection);

            for (String songIdStr : songIds) {
                int songId = Integer.parseInt(songIdStr);
                playlistDAO.addSongToPlaylist(playlistId, songId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
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
