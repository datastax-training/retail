package playlist.controller;

import playlist.model.PlaylistDAO;
import playlist.model.TracksDAO;
import playlist.model.UserDAO;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.UUID;

/**
 * DataStax Academy Sample Application
 *
 * Copyright 2013 DataStax
 *
 */

public class PlaylistTracksServlet extends HttpServlet {

  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    // We add playlists with a post method

    HttpSession httpSession = request.getSession(true);

    String button = request.getParameter("button");
    String playlist_name = request.getParameter("pl");
    UserDAO user = (UserDAO) httpSession.getAttribute("user");

    PlaylistDAO playlist = PlaylistDAO.getPlaylistForUser(user.getUsername(), playlist_name);

    if (button != null) {
      if (button.contentEquals("addTrack")) {
        UUID track_id = UUID.fromString(request.getParameter("track_id"));
        doAddPlaylistTrack(playlist, track_id);
      }
    }

    request.setAttribute("username", user.getUsername());
    request.setAttribute("playlist", playlist);
    getServletContext().getRequestDispatcher("/playlist_tracks.jsp").forward(request,response);
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    HttpSession httpSession = request.getSession(true);
    UserDAO user = (UserDAO) httpSession.getAttribute("user");

    // If we're not logged in, go to the login page
    if (user == null) {

      request.setAttribute("error", "Not Logged In");
      response.sendRedirect("login");
      return;
    }

    //
    // Initialize the parameters that are returned from the web page
    //

    String playlist_name = request.getParameter("pl");
    PlaylistDAO playlist = PlaylistDAO.getPlaylistForUser(user.getUsername(), playlist_name);

    String button = request.getParameter("button");
    String deleteTrack = request.getParameter("deleteTrack");

    //
    // If a button was pressed, carry out the button's action
    //

    if (deleteTrack != null) {

      // Delete one track
        long sequence_no = Long.parseLong(deleteTrack);
        doDeleteTrack(playlist, sequence_no);
    }

    request.setAttribute("username", user.getUsername());
    request.setAttribute("playlist", playlist);
    getServletContext().getRequestDispatcher("/playlist_tracks.jsp").forward(request,response);

  }

  void doAddPlaylistTrack(PlaylistDAO playlist, UUID track_id) throws ServletException {
    // Grab the PlaylistTrack information from the DB
    TracksDAO track = TracksDAO.getTrackById(track_id);

    PlaylistDAO.PlaylistTrack newPlaylistTrack = new PlaylistDAO.PlaylistTrack(track);
    try {
      playlist.addTrackToPlaylist(newPlaylistTrack);
    } catch (Exception e) {
      throw new ServletException("Couldn't add track to playlist");
    }
  }

  void doDeleteTrack(PlaylistDAO playlist, long sequence_no) {
      playlist.deleteTrackFromPlaylist(sequence_no);
  }

}
