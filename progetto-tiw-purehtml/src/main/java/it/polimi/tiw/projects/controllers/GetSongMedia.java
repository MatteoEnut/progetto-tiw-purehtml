package it.polimi.tiw.projects.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import it.polimi.tiw.projects.beans.Song;
import it.polimi.tiw.projects.dao.SongDAO;
import it.polimi.tiw.projects.utils.ConnectionHandler;

@WebServlet("/GetSongMedia")
public class GetSongMedia extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		int songId = Integer.parseInt(request.getParameter("id"));
		String type = request.getParameter("type"); // "image" or "audio"

		try {
			SongDAO songDAO = new SongDAO(connection);
			Song song = songDAO.findSongById(songId);
			if (song == null) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "Song not found");
				return;
			}

			byte[] data;
			String contentType;

			if ("image".equals(type)) {
				data = song.getImage();
				contentType = "image/jpeg";
			} else if ("audio".equals(type)) {
				data = song.getAudio();
				contentType = "audio/mpeg";
			} else {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid media type");
				return;
			}

			if (data == null) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "Media not available");
				return;
			}

			response.setContentType(contentType);
			response.setContentLength(data.length);
			response.getOutputStream().write(data);

		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to load media");
		}
	}

	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}

