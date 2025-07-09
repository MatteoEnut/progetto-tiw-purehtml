package it.polimi.tiw.projects.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.sql.Date;

import org.apache.commons.lang.StringEscapeUtils;

import it.polimi.tiw.projects.beans.User;
import it.polimi.tiw.projects.dao.SongDAO;
import it.polimi.tiw.projects.utils.ConnectionHandler;

/**
 * Servlet implementation class CreateSong
 */
@WebServlet("/CreateSong")
public class CreateSong extends HttpServlet {
	private static final long serialVersionUID = 1L;
    
	private Connection connection = null;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CreateSong() {
        super();
    }

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}
    
	private Date getMeTomorrow() {
		return new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000);
	}
    
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// If the user is not logged in (not present in session) redirect to the login
		HttpSession session = request.getSession();
		if (session.isNew() || session.getAttribute("user") == null) {
			String loginpath = getServletContext().getContextPath() + "/index.html";
			response.sendRedirect(loginpath);
			return;
		}
		
		boolean isBadRequest = false;
		String title = null, album = null, artist = null, genre = null;
		Date date = null;
		byte[] audioBytes = null, imageBytes = null;

		try {
		    Part audioPart = request.getPart("audio");
		    Part imagePart = request.getPart("image");

		    title = StringEscapeUtils.escapeJava(request.getParameter("title"));
		    album = StringEscapeUtils.escapeJava(request.getParameter("album"));
		    artist = StringEscapeUtils.escapeJava(request.getParameter("artist"));
		    genre = StringEscapeUtils.escapeJava(request.getParameter("genre"));

		    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		    date = (Date) sdf.parse(request.getParameter("date"));
		    
		    if (audioPart != null && audioPart.getSize() > 0)
		        audioBytes = audioPart.getInputStream().readAllBytes();
		    if (imagePart != null && imagePart.getSize() > 0)
		        imageBytes = imagePart.getInputStream().readAllBytes();

		    isBadRequest = title == null || title.isEmpty()
		        || album == null || album.isEmpty()
		        || artist == null || artist.isEmpty()
		        || genre == null || genre.isEmpty()
		        || date == null || getMeTomorrow().before(date)
		        || audioBytes == null || audioBytes.length == 0
		        || imageBytes == null || imageBytes.length == 0;

		} catch (Exception e) {
		    isBadRequest = true;
		    e.printStackTrace();  // for debugging
		}

		if (isBadRequest) {
		    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing or incorrect song data");
		    return;
		}
		
		User user = (User) session.getAttribute("user");
		SongDAO songDAO = new SongDAO(connection);

		// Attempt to create the song in the DB
		try {
		    songDAO.createSong(title, album, artist, date, genre, imageBytes, audioBytes, user.getUsername());
		} catch (SQLException e) {
		    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to create song");
		    return;
		}

		// Redirect to home after successful creation
		String ctxpath = getServletContext().getContextPath();
		String path = ctxpath + "/Home";
		response.sendRedirect(path);
	}

}
