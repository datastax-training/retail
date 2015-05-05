package playlist.model;

import com.datastax.driver.core.*;
import playlist.exceptions.UserExistsException;
import playlist.exceptions.UserLoginException;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

/**
 * DataStax Academy Sample Application
 *
 * Copyright 2013 DataStax
 *
 */


public class UserDAO extends CassandraData {

  private String username;
  private String password;
  private SortedSet<String> playlist_names;

  private UserDAO(Row row) {
    username = row.getString("username");
    password = row.getString("password");

    // We do this because we want a sorted set, and Cassandra only returns a regular set
    // the driver gives us a HashLinkedSet. We need to choose our implementation.
    playlist_names = new TreeSet<>();
    playlist_names.addAll(row.getSet("playlist_names", String.class));

  }

  UserDAO(String username, String password) {
    this.password = password;
    this.username = username;
    this.playlist_names = new TreeSet<>();
  }

  /**
   * Static method to add a user.  It generates a new UUID for the user to use as its surrogate key.  It first checks to
   * ensure that the user does not already exist.  If it exists, we throw a UserExistsException
   *
   * @param username new user's username address
   * @param password new user's password
   * @return a UserDAO object
   * @throws UserExistsException
   */
  public static UserDAO addUser(String username, String password) throws UserExistsException {

    String queryText = "INSERT INTO users (username, password) values (?, ?) IF NOT EXISTS";

    PreparedStatement preparedStatement = getSession().prepare(queryText);

    // Because we use an IF NOT EXISTS clause, we get back a result set with 1 row containing 1 boolean column called "[applied]"
    ResultSet resultSet = getSession().execute(preparedStatement.bind(username, password));

    // Determine if the user was inserted.  If not, throw an exception.
    boolean userGotInserted = resultSet.one().getBool("[applied]");

    if (!userGotInserted) {
      throw new UserExistsException();
    }

    // Return the new user so the caller can get the userid
    return new UserDAO(username, password);

  }

  /**
   * Delete the user.  It does not need to check if the user already exists.
   */
  public void deleteUser() {
    SimpleStatement simpleStatement = new SimpleStatement("DELETE FROM users where username = '"
            + this.username + "'");

    // Delete users with CL = Quorum
    simpleStatement.setConsistencyLevel(ConsistencyLevel.QUORUM);
    getSession().execute(simpleStatement);

  }


  /**
   * Look up a user by e-mail address and return a UserDAO object for it
   *
   * @param username The username address to search
   * @return  A full UserDAO object
   */
  public static UserDAO getUser(String username) {
    String queryText = "SELECT * FROM users where username = '"
            + username + "'";

    Row userRow = getSession().execute(queryText).one();

    if (userRow == null) {
      return null;
    }

    return new UserDAO(userRow);  }


  /**
   * This routine retrieves a userDAO, but reads it with a consistency level of quorum
   *
   * @param username username address to search
   * @return a UserDAO object
   */
  private static UserDAO getUserWithQuorum(String username) {

    String queryText = "SELECT * FROM users where username = '"
            + username + "'";

    SimpleStatement simpleStatement = new SimpleStatement(queryText);
    simpleStatement.setConsistencyLevel(ConsistencyLevel.QUORUM);
    Row userRow = getSession().execute(simpleStatement).one();

    if (userRow == null) {
      return null;
    }

    return new UserDAO(userRow);

  }

  /**
   * The also retrieves a user based on the username address, but also validates its password.  If the password is invalid, a
   * UserLoginException is thrown
   *
   * @param username  username address to search
   * @param password  password to validate
   * @return  a completed UserDAO object
   * @throws UserLoginException
   */
  public static UserDAO validateLogin(String username, String password) throws UserLoginException {

    UserDAO user = getUserWithQuorum(username);
    if (user == null || !user.password.contentEquals(password)) {
      throw new UserLoginException();
    }

    return user;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public Set<String> getPlaylist_names() {
    return playlist_names;
  }


}
