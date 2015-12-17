package retail.helpers.cassandra;

import com.datastax.driver.core.*;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import org.apache.commons.lang3.text.WordUtils;

import java.lang.reflect.Method;

/**
 * DataStax Academy Sample Application
 *
 * Copyright 2013 DataStax
 *
 * This is a Singleton class that holds 1 Cassandra session that all requests will share.
 * It has 1 public method to return that session.
 *
 *
 *
 *
 */

public class CassandraData {

  //
  // A static variable that holds the session.  Only one of these will exist for the whole application
  //

  private static Session cassandraSession = null;
  private static MappingManager mappingManager = null;


  /**
   * Required constructor, but it doesn't need to do anything.
   */

  public CassandraData() {
    // Do nothing
  }

  /**
   * Return the Cassandra session.
   * When the application starts up, the session
   * is set to null.  When this function is called, it checks to see if the session is null.
   * If so, it creates a new session, and sets the static session.
   * <p/>
   * All of the DAO classes are subclasses of this
   *
   * @return - a valid cassandra session
   */


  public static void init(String keyspace, String ... contactpoints) {

    Cluster cluster = Cluster.builder().addContactPoints(contactpoints).build();
    cassandraSession = cluster.connect(keyspace);
    mappingManager = new MappingManager(cassandraSession);

  }

  public static Session getSession() {

    return cassandraSession;

  }

  // Get an object mapper for a particular type
  public static <T> Mapper<T> getMappingManager(Class<T> clazz) {
    return mappingManager.mapper(clazz);
  }


  /**
   * Create a new cassandra Cluster() and Session().  Returns the Session.
   *
   * @return A new Cassandra session
   */

  public static String makeSolrQueryString(String search_term, String filter_by) {
    String solr_query = "\"q\":\"title:" + escapeQuotes(search_term) + "\"";

    if (filter_by != null && !filter_by.isEmpty()) {
      solr_query += ",\"fq\":\"" + escapeQuotes(filter_by) + "\"";
    }
    return solr_query;
  }

  private static String escapeQuotes(String s) {
    if (s == null) {
      return null;
    }
    return s.replace("\"", "\\\"");
  }

}