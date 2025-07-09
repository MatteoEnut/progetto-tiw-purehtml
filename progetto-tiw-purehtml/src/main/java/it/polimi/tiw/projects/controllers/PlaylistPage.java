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

/**
 * Servlet implementation class PlaylistPage
 */
@WebServlet("/Playlist")
public class PlaylistPage extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	private TemplateEngine templateEngine;
	private Connection connection = null;
	private JakartaServletWebApplication webApp;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
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
	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// If the user is not logged in (not present in session) redirect to the login
	    String loginpath = getServletContext().getContextPath() + "/index.html";
	    HttpSession session = request.getSession();
	    if (session.isNew() || session.getAttribute("user") == null) {
	        response.sendRedirect(loginpath);
	        return;
	    }
		
	    int id = Integer.parseInt(request.getParameter("id"));
	    List<Song> songs = new ArrayList<>();
	    Playlist playlist = null;
	    
	    // devo controllare se l'id è mio
	    
	    try {
	        PlaylistDAO playlistDAO = new PlaylistDAO(connection);
	        songs = playlistDAO.findSongsByPlaylist(id);
	        playlist = playlistDAO.findPlaylistById(id);
	    } catch (Exception e) {
	        e.printStackTrace(); 
	    }

	    final WebContext ctx = new WebContext(webApp.buildExchange(request, response), request.getLocale());
	    ctx.setVariable("songs", songs);
	    ctx.setVariable("playlist", playlist);
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
