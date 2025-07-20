package it.polimi.tiw.projects.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.polimi.tiw.projects.beans.Song;

public class SongDAO {
	
	private Connection conn;
	
	public SongDAO (Connection connection) {
		this.conn = connection;
	}
	
	public void createSong (
			String title, String album, String artist, Date date, 
			String genre, byte[] image, byte[] audio, String username
			) throws SQLException {
		
		String query = "INSERT INTO song (title, album, artist, date, genre, image, audio, username) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		try (PreparedStatement pStatement = conn.prepareStatement(query);) {
			pStatement.setString(1, title);
			pStatement.setString(2, album);
			pStatement.setString(3, artist);
			pStatement.setDate(4, date);
			pStatement.setString(5, genre);
			pStatement.setBytes(6, image);
			pStatement.setBytes(7, audio);
			pStatement.setString(8, username);
			pStatement.executeUpdate();
		} catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	public List<Song> findSongsByUser(String username) throws SQLException {
		List<Song> songs = new ArrayList<Song>();
		
		String query = "SELECT * FROM song WHERE username = ? ORDER BY artist ASC, date ASC";
		try (PreparedStatement pstatement = conn.prepareStatement(query);) {
			pstatement.setString(1, username);
			try (ResultSet result = pstatement.executeQuery();) {
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
	
	public List<Song> findSongsByUserNoData(String username) throws SQLException {
	    List<Song> songs = new ArrayList<>();

	    String query = "SELECT id, title, album, artist, date, genre, username FROM song WHERE username = ? ORDER BY artist ASC, date ASC";
	    try (PreparedStatement pstatement = conn.prepareStatement(query)) {
	        pstatement.setString(1, username);
	        try (ResultSet result = pstatement.executeQuery()) {
	            while (result.next()) {
	                Song song = new Song();
	                song.setId(result.getInt("id"));
	                song.setTitle(result.getString("title"));
	                song.setAlbum(result.getString("album"));
	                song.setArtist(result.getString("artist"));
	                song.setDate(result.getDate("date"));
	                song.setGenre(result.getString("genre"));
	                song.setUsername(result.getString("username"));

	                songs.add(song);
	            }
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return songs;
	}
	
	public Song findSongById(int id) throws SQLException {
		Song song = null;

		String query = "SELECT * FROM song WHERE id = ?";
		try (PreparedStatement pstatement = conn.prepareStatement(query)) {
			pstatement.setInt(1, id);
			try (ResultSet result = pstatement.executeQuery()) {
				if (result.next()) {
					song = new Song();
					song.setId(result.getInt("id"));
					song.setTitle(result.getString("title"));
					song.setAlbum(result.getString("album"));
					song.setArtist(result.getString("artist"));
					song.setDate(result.getDate("date"));
					song.setGenre(result.getString("genre"));
					song.setImage(result.getBytes("image"));
					song.setAudio(result.getBytes("audio"));
					song.setUsername(result.getString("username"));
				}
			}
		} catch (SQLException e) {
	        e.printStackTrace();
	    }

		return song;
	}
	
	public Song findSongByIdNoData(int id) throws SQLException {
		Song song = null;

		String query = "SELECT id, title, album, artist, date, genre, username FROM song WHERE id = ?";
		try (PreparedStatement pstatement = conn.prepareStatement(query)) {
			pstatement.setInt(1, id);
			try (ResultSet result = pstatement.executeQuery()) {
				if (result.next()) {
					song = new Song();
					song.setId(result.getInt("id"));
					song.setTitle(result.getString("title"));
					song.setAlbum(result.getString("album"));
					song.setArtist(result.getString("artist"));
					song.setDate(result.getDate("date"));
					song.setGenre(result.getString("genre"));
					song.setUsername(result.getString("username"));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return song;
	}
	
	public byte[] findSongAudioById(int songId) throws SQLException {
	    byte[] audio = null;

	    String query = "SELECT audio FROM song WHERE id = ?";
	    try (PreparedStatement pstatement = conn.prepareStatement(query)) {
	        pstatement.setInt(1, songId);
	        try (ResultSet result = pstatement.executeQuery()) {
	            if (result.next()) {
	                audio = result.getBytes("audio");
	            }
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return audio;
	}

	public byte[] findSongImageById(int songId) throws SQLException {
	    byte[] image = null;

	    String query = "SELECT image FROM song WHERE id = ?";
	    try (PreparedStatement pstatement = conn.prepareStatement(query)) {
	        pstatement.setInt(1, songId);
	        try (ResultSet result = pstatement.executeQuery()) {
	            if (result.next()) {
	                image = result.getBytes("image");
	            }
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return image;
	}

	
	
}
