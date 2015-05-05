package playlist.model;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;

import java.util.ArrayList;
import java.util.List;

/**
 * DataStax Academy Sample Application
 *
 * Copyright 2013 DataStax
 *
 * Handles the collection of counter statistics for the application.  It has methods to increment and decrement
 * counters in Cassandra, as well as return a list of them
 *
 */


public class StatisticsDAO extends CassandraData {

  private final String counter_name;
  private final long counter_value;

  private StatisticsDAO(Row row) {
    counter_name = row.getString("counter_name");
    counter_value = row.getLong("counter_value");
  }

  // Static finder method

  /**
   *
   * Retrieve the statistics
   *
   * @return - Return a List of Statistics objects. Each includes
   * the counter name and its value.
   *
   */

  public static List<StatisticsDAO> getStatistics() {

    String queryText = "SELECT * FROM statistics";
    ResultSet results = getSession().execute(queryText);

    List<StatisticsDAO> statistics = new ArrayList<>();

    for (Row row : results) {
       statistics.add(new StatisticsDAO(row));     // Lets use column 0 since there is only one column
    }

    return statistics;
  }

  /**
   *
   * Increment a statistics counter
   *
   * @param counter_name  Counter to Increment
   */

  public static void increment_counter(String counter_name) {

    String queryText = "UPDATE statistics set counter_value = counter_value + 1 where counter_name = '" + counter_name +"'";
    getSession().execute(queryText);

  }

  /**
   *
   * Decrement a statistics counter
   * @param counter_name Counter to Decrement
   *
   */

  public static void decrement_counter(String counter_name) {

    String queryText = "UPDATE statistics set counter_value = counter_value - 1 where counter_name = '" + counter_name +"'";
    getSession().execute(queryText);

  }

  public String getCounter_name() {
    return counter_name;
  }

  public long getCounter_value() {
    return counter_value;
  }
}
