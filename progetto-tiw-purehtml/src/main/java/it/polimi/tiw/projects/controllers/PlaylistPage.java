package it.polimi.tiw.projects.controllers;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.Console;
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
	    
	    String songErrorMsg = (String) request.getSession().getAttribute("songErrorMsg");
	    request.getSession().removeAttribute("songErrorMsg");
	    request.setAttribute("songErrorMsg", songErrorMsg);
	    
	    int playlistId = Integer.parseInt(request.getParameter("id"));
	    int page = 0;
	    final int pageSize = 5;

	    String pageParam = request.getParameter("page");
	    if (pageParam != null) {
	        try {
	            page = Integer.parseInt(pageParam);
	        } catch (NumberFormatException e) {
	            page = 0;
	        }
	    }

	    List<Song> pagedSongs = new ArrayList<>();
	    List<Song> allUserSongs = new ArrayList<>();
	    Playlist playlist = null;
	    
        final WebContext ctx = new WebContext(webApp.buildExchange(request, response), request.getLocale());
	    
	    try {
	        PlaylistDAO playlistDAO = new PlaylistDAO(connection);
	        SongDAO songDAO = new SongDAO(connection);
	        User user = (User) session.getAttribute("user");
	        
	        playlist = playlistDAO.findPlaylistById(playlistId);
	        
	        if (playlist == null || !playlist.getUsername().equals(user.getUsername())) {
	            //response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
	            String ctxpath = getServletContext().getContextPath();
	    		String path = ctxpath + "/Home";
	            response.sendRedirect(path);
	        	return;
	        }
	        
	        //int totalSongs = playlistDAO.countSongsInPlaylist(playlistId);
	        List<Song> allSongsInPlaylist = playlistDAO.findSongsByPlaylist(playlistId);
	        int totalSongs = allSongsInPlaylist.size();
	        
	        int offset = page * pageSize;
	        pagedSongs = playlistDAO.findSongsByPlaylistPaged(playlistId, offset, pageSize);

	        allUserSongs = songDAO.findSongsByUser(user.getUsername());

	       
	        allUserSongs.removeIf(song -> allSongsInPlaylist.stream().anyMatch(s -> s.getId() == song.getId()));

	        boolean hasPrevious = page > 0;
	        boolean hasNext = offset + pageSize < totalSongs;

	        ctx.setVariable("songs", pagedSongs);
	        ctx.setVariable("playlist", playlist);
	        ctx.setVariable("availableSongs", allUserSongs);
	        ctx.setVariable("page", page);
	        ctx.setVariable("hasPrevious", hasPrevious);
	        ctx.setVariable("hasNext", hasNext);

	        templateEngine.process("/WEB-INF/templates/playlist.html", ctx, response.getWriter());

	    } catch (Exception e) {
	        //e.printStackTrace();
	        //response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to load playlist or songs");
	    	ctx.setVariable("errorMsg", "Unable to load playlist or songs");
		    templateEngine.process("/WEB-INF/templates/playlist.html", ctx, response.getWriter());
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
