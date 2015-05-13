package retail.helpers.cassandra;

import com.datastax.driver.core.*;
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
  }

  public static Session getSession() {

    return cassandraSession;

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

  // Looks at the row, and sets all of the fields in this object.
  // the setter in the object must be CamelCase eg. setProductID

  public void loadBeanFromRow(Row row) {
      for (ColumnDefinitions.Definition column_def: row.getColumnDefinitions()) {

        // Generate the setter name
         String camelCaseName = getCamelCaseName(column_def);
         String setter = "set" + camelCaseName;
        try {
          Method method = this.getClass().getMethod(setter, column_def.getType().asJavaClass());
          Object data_object = column_to_object(row, column_def.getName());
          method.invoke(this, data_object);
        } catch (Exception e) {
          // We assume it's a column in the DB with no field, or the setter has the wrong
          // name
        }
      }
  }

  private String getCamelCaseName(ColumnDefinitions.Definition column_def) {
    return WordUtils.capitalize(column_def.getName().replace('_', ' ')).replace(" ","");
  }

  public static Object column_to_object(Row row, String column_name) {
    int index = row.getColumnDefinitions().getIndexOf(column_name);
    return column_to_object(row,index);
  }

  public static Object column_to_object(Row row, int index) {

    DataType cassandra_type = row.getColumnDefinitions().getType(index);
    Class cassandra_clazz = cassandra_type.asJavaClass();

    if (row.isNull(index)) {
      return null;
    }

    String type_name = cassandra_clazz.getName();
    switch (type_name) {
      case "java.lang.Integer":
        return row.getInt(index);
      case "java.lang.Double":
        return row.getDouble(index);
      case "java.lang.float":
        return row.getFloat(index);
      case "java.lang.Long":
        return row.getLong(index);
      case "java.math.BigDecimal":
        return row.getDecimal(index);
      case "java.math.BigInteger":
        return row.getVarint(index);
      case "java.lang.Boolean":
        return row.getBool(index);
      case "java.util.Date":
        return row.getDate(index);   // treat as string
      case "java.lang.String":
        return row.getString(index);
      case "java.util.UUID":
        return row.getUUID(index);
      case "java.util.Map":
        return row.getMap(index,
                cassandra_type.getTypeArguments().get(0).asJavaClass(),
                cassandra_type.getTypeArguments().get(1).asJavaClass()
                );
      case "java.util.Set":
        return row.getSet(index, cassandra_type.getTypeArguments().get(0).asJavaClass());
      case "java.util.List":
        return row.getList(index, cassandra_type.getTypeArguments().get(0).asJavaClass());
      default:
        throw new UnsupportedOperationException(type_name + "is not supported.");
    }
  }
}