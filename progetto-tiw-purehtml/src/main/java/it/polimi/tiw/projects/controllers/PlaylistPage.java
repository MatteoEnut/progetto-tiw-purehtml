package it.polimi.tiw.projects.controllers;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import it.polimi.tiw.projects.beans.Playlist;
import it.polimi.tiw.projects.beans.Song;
import it.polimi.tiw.projects.beans.User;
import it.polimi.tiw.projects.dao.PlaylistDAO;
import it.polimi.tiw.projects.dao.SongDAO;
import it.polimi.tiw.projects.utils.ConnectionHandler;


@WebServlet("/Playlist")
public class PlaylistPage extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	private TemplateEngine templateEngine;
	private Connection connection = null;
	private JakartaServletWebApplication webApp;
	
	
    public PlaylistPage() {
        super();
    }

    
	public void init() throws ServletException {
	    ServletContext servletContext = getServletContext();
	    webApp = JakartaServletWebApplication.buildApplication(servletContext);
	    WebApplicationTemplateResolver templateResolver = new WebApplicationTemplateResolver(webApp);
	    templateResolver.setTemplateMode(TemplateMode.HTML);
	    templateResolver.setSuffix(".html");

	    this.templateEngine = new TemplateEngine();
	    this.templateEngine.setTemplateResolver(templateResolver);

	    connection = ConnectionHandler.getConnection(getServletContext());
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    String loginpath = getServletContext().getContextPath() + "/index.html";
	    HttpSession session = request.getSession();
	    if (session.isNew() || session.getAttribute("user") == null) {
	        response.sendRedirect(loginpath);
	        return;
	    }

	    int id = Integer.parseInt(request.getParameter("id"));
	    List<Song> songs = new ArrayList<>();
	    Playlist playlist = null;
	    List<Song> allUserSongs = new ArrayList<>();
	    
	    // devo controllare se l'id è mio

	    try {
	        PlaylistDAO playlistDAO = new PlaylistDAO(connection);
	        SongDAO songDAO = new SongDAO(connection);

	        playlist = playlistDAO.findPlaylistById(id);
	        songs = playlistDAO.findSongsByPlaylist(id);

	        User user = (User) session.getAttribute("user");
	        allUserSongs = songDAO.findSongsByUser(user.getUsername());

	        final List<Song> songsInPlaylist = songs;
	        // Rimuovo i brani già nella playlist
	        allUserSongs.removeIf(song -> songsInPlaylist.stream().anyMatch(s -> s.getId() == song.getId()));

	    } catch (Exception e) {
	        e.printStackTrace();
	        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to load playlist or songs");
	    }
	    
	    final WebContext ctx = new WebContext(webApp.buildExchange(request, response), request.getLocale());
        ctx.setVariable("songs", songs);
        ctx.setVariable("playlist", playlist);
        ctx.setVariable("availableSongs", allUserSongs);

        templateEngine.process("/WEB-INF/templates/playlist.html", ctx, response.getWriter());
	}

	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
