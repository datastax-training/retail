package playlist.model;

import junit.framework.TestCase;
import java.util.List;

/**
 * DataStax Academy Sample Application
 *
 * Copyright 2013 DataStax
 *
 */


public class ArtistTest extends TestCase {

  public void testFindArtistsStartingWithA() throws Exception {

    List<String> artists = AdHocDAO.listArtistByLetter("A", false);

    assertEquals(245, artists.size());

    // Check the first artist
    String firstArtist = artists.get(0);

    assertEquals("A Certain Ratio", firstArtist);

  }

  public void testFindArtistsStartingWithADesc() throws Exception {

    List<String> artists = AdHocDAO.listArtistByLetter("A", true);

    assertEquals(245, artists.size());

    // Check the first artist
    String firstArtist = artists.get(0);

    assertEquals("}AMA & RNB WANNABES", firstArtist);

  }

  public void testFindArtistsStartingWithInvalidLetter() throws Exception {

    List<String> artists = AdHocDAO.listArtistByLetter("=", false);

    assertEquals(0, artists.size());

  }

}
