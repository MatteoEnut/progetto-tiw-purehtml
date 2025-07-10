package it.polimi.tiw.projects.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntBinaryOperator;

import com.mysql.cj.x.protobuf.MysqlxCrud.Insert;

import it.polimi.tiw.projects.beans.Playlist;
import it.polimi.tiw.projects.beans.Song;

public class PlaylistDAO {
	private Connection connection;
	
	public PlaylistDAO(Connection connection) {
		this.connection = connection;
	}
	
	public int createPlaylist(String title, Date date, String username) throws SQLException {
		
		String query = "INSERT into Playlist (title, date, username) VALUES (?,?,?)";
		
		try (PreparedStatement pstatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);) {
			pstatement.setString(1, title);
			pstatement.setDate(2, date);
			pstatement.setString(3, username);
			pstatement.executeUpdate();
			
			try (ResultSet generatedKeys = pstatement.getGeneratedKeys()) {
				if (generatedKeys.next()) {
					return generatedKeys.getInt(1);
				} else {
					throw new SQLException("Creating playlist failed, no ID obtained.");
				}
			}
		}
	}

	public void addSongToPlaylist(int playlistID, int songID) throws SQLException {
		
		String query = "INSERT into Playlist_Song (playlist_id, song_id) VALUES (?,?)";
		
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, playlistID);
			pstatement.setInt(2, songID);
			pstatement.executeUpdate();			
		} catch (SQLException e) {
	        e.printStackTrace(); 
	    }
	}
	
	public List<Song> findSongsByPlaylist(int playlistID) {
	    List<Song> songs = new ArrayList<>();

	    String query = "SELECT s.* FROM Song s " +
	                   "JOIN Playlist_Song ps ON s.id = ps.song_id " +
	                   "WHERE ps.playlist_id = ?";

	    try (PreparedStatement pstatement = connection.prepareStatement(query)) {
	    	pstatement.setInt(1, playlistID);

	        try (ResultSet result = pstatement.executeQuery()) {
	            while (result.next()) {
	                Song song = new Song();
	                song.setId(result.getInt("id"));
	                song.setTitle(result.getString("title"));
	                song.setAlbum(result.getString("album"));
	                song.setArtist(result.getString("artist"));
	                song.setDate(result.getDate("date"));
	                song.setGenre(result.getString("genre"));
	                song.setImage(result.getBytes("image"));
	                song.setAudio(result.getBytes("audio"));
	                song.setUsername(result.getString("username"));

	                songs.add(song);
	            }
	        }
	    } catch (SQLException e) {
	        e.printStackTrace(); 
	    }

	    return songs;
	}
	
	public List<Playlist> findPlaylistsByUser(String username) {
	    List<Playlist> playlists = new ArrayList<>();

	    String query = "SELECT * FROM Playlist WHERE username = ? ORDER BY date DESC";

	    try (PreparedStatement pstatement = connection.prepareStatement(query)) {
	    	pstatement.setString(1, username);

	        try (ResultSet result = pstatement.executeQuery()) {
	            while (result.next()) {
	                Playlist playlist = new Playlist();
	                playlist.setId(result.getInt("id"));
	                playlist.setTitle(result.getString("title"));
	                playlist.setDate(result.getDate("date"));
	                playlist.setUsername(result.getString("username"));

	                playlists.add(playlist);
	            }
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return playlists;
	}

	public Playlist findPlaylistById(int playlistID) {
	    Playlist playlist = null;

	    String query = "SELECT * FROM Playlist WHERE id = ?";

	    try (PreparedStatement pstatement = connection.prepareStatement(query)) {
	    	pstatement.setInt(1, playlistID);

	        try (ResultSet result = pstatement.executeQuery()) {
	            if (result.next()) {
	                playlist = new Playlist();
	                playlist.setId(result.getInt("id"));
	                playlist.setTitle(result.getString("title"));
	                playlist.setDate(result.getDate("date"));
	                playlist.setUsername(result.getString("username"));
	            }
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return playlist;
	}

}
