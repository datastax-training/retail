package playlist.model;

import com.datastax.driver.core.Session;
import junit.framework.TestCase;
import playlist.exceptions.UserExistsException;
import playlist.exceptions.UserLoginException;

/**
 * DataStax Academy Sample Application
 *
 * Copyright 2013 DataStax
 *
 */

public class UserTest extends TestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    Session session  = CassandraData.getSession();
    session.execute("TRUNCATE users");
    session.execute("TRUNCATE playlist_tracks");

  }


  public void testInsertUser() throws Exception {

    UserDAO.addUser("steve", "iforgot");

  }
  public void testGetUser() throws Exception {

    UserDAO.addUser("steve", "iforgot");
    UserDAO user = UserDAO.getUser("steve");

    assertEquals("steve",user.getUsername());
    assertEquals("iforgot",user.getPassword());

    user.deleteUser();

  }

  public void testDeleteUser() throws Exception {

    UserDAO newUser = UserDAO.addUser("steve", "iforgot");

    newUser.deleteUser();

    UserDAO user = UserDAO.getUser("steve");

    assertNull("user should be null", user);

  }

  public void testAddSameUserTwice() throws Exception {

    UserDAO.addUser("steve", "pw1");
    UserDAO user = UserDAO.getUser("steve");
    assertEquals("pw1",user.getPassword());

    boolean thrown = false;
    try {
      UserDAO.addUser("steve", "pw2");
    } catch (UserExistsException e) {
      thrown = true;
    }

    assertTrue("UserExistsException not thrown", thrown);

    user = UserDAO.getUser("steve");
    assertEquals("pw1",user.getPassword());

    user.deleteUser();

  }

  public void testValidateLogin() throws Exception {

    UserDAO.addUser("steve", "pw1");
    UserDAO user = UserDAO.getUser("steve");
    assertEquals("pw1",user.getPassword());

    UserDAO loginUser = UserDAO.validateLogin("steve", "pw1");
    assertNotNull(loginUser);

    user.deleteUser();

  }

  public void testValidateBadPassword() throws Exception {

    UserDAO newUser = UserDAO.addUser("steve", "pw1");
    UserDAO user = UserDAO.getUser("steve");
    assertEquals("pw1",user.getPassword());

    boolean thrown = false;
    try {
      UserDAO.validateLogin("steve", "badpassword");
    } catch (UserLoginException e) {
          thrown = true;
    }

    assertTrue("exception not thrown for bad login", thrown);

    newUser.deleteUser();

  }

  public void testValidateBadUsername() throws Exception {

    UserDAO newUser = UserDAO.addUser("steve", "pw1");
    UserDAO user = UserDAO.getUser("steve");
    assertEquals("pw1",user.getPassword());

    boolean thrown = false;
    try {
      UserDAO.validateLogin("baduser", "pw1");
    } catch (UserLoginException e) {
      thrown = true;
    }

    assertTrue("exception not thrown for bad login", thrown);

    newUser.deleteUser();

  }

}
