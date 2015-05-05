package playlist.model;

import com.datastax.driver.core.Session;
import junit.framework.TestCase;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * DataStax Academy Sample Application
 *
 * Copyright 2013 DataStax
 *
 */

public class PlaylistDAOTest extends TestCase {


  @Override
  protected void setUp() throws Exception {
    super.setUp();

    Session session  = CassandraData.getSession();
    session.execute("TRUNCATE users");
    session.execute("TRUNCATE playlist_tracks");

  }

  public void testAddTrackToPlaylist() throws Exception {
    UserDAO user = UserDAO.addUser("testuser1","pw");
    PlaylistDAO newPlaylist = PlaylistDAO.createPlayList(user,"Playlist1");

    TracksDAO newTrack = new TracksDAO("Artist1","Track1", "rock", "music file 1", 20);
    PlaylistDAO.PlaylistTrack playlistTrack1 = new PlaylistDAO.PlaylistTrack(newTrack);

    newPlaylist.addTrackToPlaylist(playlistTrack1);

    PlaylistDAO playlist = PlaylistDAO.getPlaylistForUser(user.getUsername(), "Playlist1");
    List<PlaylistDAO.PlaylistTrack> tracksList = playlist.getPlaylistTrackList();

    assertEquals(1, tracksList.size());

    UUID uuid2 = UUID.randomUUID();
    newTrack = new TracksDAO("Artist2","Track2", "rock", "music fiile 2", 10);
    PlaylistDAO.PlaylistTrack playlistTrack2 = new PlaylistDAO.PlaylistTrack(newTrack);
    playlist.addTrackToPlaylist(playlistTrack2);
    playlist = PlaylistDAO.getPlaylistForUser(user.getUsername(), "Playlist1");
    tracksList = playlist.getPlaylistTrackList();

    assertEquals(2, tracksList.size());

    assertEquals(30, playlist.getPlaylist_length_in_seconds());

    playlist.deletePlayList();
    user.deleteUser();
  }

  public void testDeleteTrackFromPlaylist() throws Exception {
    UserDAO user = UserDAO.addUser("testuser1","pw");
    PlaylistDAO newPlaylist = PlaylistDAO.createPlayList(user, "Playlist1");

    TracksDAO newTrack = new TracksDAO("Artist1","Track1", "rock", "music file 3", 20);
    PlaylistDAO.PlaylistTrack playlistTrack1 = new PlaylistDAO.PlaylistTrack(newTrack);

    newPlaylist.addTrackToPlaylist(playlistTrack1);

    PlaylistDAO playlist = PlaylistDAO.getPlaylistForUser(user.getUsername(), "Playlist1");
    List<PlaylistDAO.PlaylistTrack> tracksList = playlist.getPlaylistTrackList();

    assertEquals(1, tracksList.size());
    assertEquals(20, playlist.getPlaylist_length_in_seconds());

    playlist.deleteTrackFromPlaylist(tracksList.get(0).getSequence_no());
    assertEquals(0, playlist.getPlaylist_length_in_seconds());

    playlist.deletePlayList();
    user.deleteUser();

  }

  public void testDeletePlayList() throws Exception {

    UserDAO user = UserDAO.addUser("testuser1","pw");
    PlaylistDAO pl = PlaylistDAO.createPlayList(user,"Bob's favorites");

    assertEquals("testuser1",pl.getUsername() );

    UserDAO testUser = UserDAO.getUser("testuser1");
    Set<String> playlists = testUser.getPlaylist_names();

    assertTrue("playlist exists", playlists.contains("Bob's favorites"));

    pl.deletePlayList();
    testUser = UserDAO.getUser("testuser1");
    playlists = testUser.getPlaylist_names();

    assertFalse("playlist exists", playlists.contains("Bob's favorites"));

    user.deleteUser();

  }

  public void testAddPlayList() throws Exception {
    UserDAO user = UserDAO.addUser("testuser1","pw");
    PlaylistDAO pl = PlaylistDAO.createPlayList(user, "Bob's favorites");

    assertEquals("testuser1",pl.getUsername() );

    UserDAO testUser = UserDAO.getUser("testuser1");
    Set<String> playlists = testUser.getPlaylist_names();

    assertTrue("playlist exists", playlists.contains("Bob's favorites"));

    user.deleteUser();

  }

}
