package it.polimi.tiw.projects.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

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

@WebServlet("/Home")
public class GoToHomePage extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;
	private Connection connection = null;
	private JakartaServletWebApplication webApp;

	public GoToHomePage() {
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

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
	        throws ServletException, IOException {
	    String loginpath = getServletContext().getContextPath() + "/index.html";
	    HttpSession session = request.getSession();
	    if (session.isNew() || session.getAttribute("user") == null) {
	        response.sendRedirect(loginpath);
	        return;
	    }

	    final WebContext ctx = new WebContext(webApp.buildExchange(request, response), request.getLocale());
	    
	    User user = (User) session.getAttribute("user");
	    List<Playlist> playlists = new ArrayList<>();
	    List<Song> userSongs = new ArrayList<>();
	    
	    String songErrorMsg = (String) request.getSession().getAttribute("songErrorMsg");
	    request.getSession().removeAttribute("songErrorMsg");
	    request.setAttribute("songErrorMsg", songErrorMsg);
	    
	    String playlistErrorMsg = (String) request.getSession().getAttribute("playlistErrorMsg");
	    request.getSession().removeAttribute("playlistErrorMsg");
	    request.setAttribute("playlistErrorMsg", playlistErrorMsg);
	    
	    boolean failed = false;
	    
	    try {
	        PlaylistDAO playlistDAO = new PlaylistDAO(connection);
	        playlists = playlistDAO.findPlaylistsByUser(user.getUsername());

	    } catch (Exception e) {
	        //e.printStackTrace();
	        //response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to load playlists or songs");
	    	ctx.setVariable("playlistListErrorMsg", "Unable to load playlists");
		    //templateEngine.process("/WEB-INF/templates/home.html", ctx, response.getWriter());
	    	return;
	    }

	    try {
	        SongDAO songDAO = new SongDAO(connection);
	        userSongs = songDAO.findSongsByUserNoData(user.getUsername());
	    }
	    catch (Exception e) {
	    	ctx.setVariable("songListErrorMsg", "Unable to load songs");
		    //templateEngine.process("/WEB-INF/templates/home.html", ctx, response.getWriter());
	    	return;
	    }
	    
	    ctx.setVariable("playlists", playlists);
	    ctx.setVariable("userSongs", userSongs);

	    templateEngine.process("/WEB-INF/templates/home.html", ctx, response.getWriter());
	}



	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
