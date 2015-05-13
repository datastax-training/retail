package retail.model;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import org.apache.commons.lang3.time.DateUtils;
import retail.helpers.cassandra.CassandraData;

import java.util.Date;

/**
 * DataStax Academy Sample Application
 *
 * Copyright 2013 DataStax
 *
 * This class returns various data about the Cassandra cluster to which the application is connected
 *
 */

public class TimeSliceDAO extends CassandraData {

  private static PreparedStatement timeslice_query = null;

  /**
   * Constructor to query cassandra for the release_version and cluster_name
   */

  public static ResultSet getTimeSlice (String series, int minutes) {
    Date end_time = new Date();
    Date start_time = DateUtils.addMinutes(end_time, -minutes);

    if (timeslice_query == null) {
      String statement = "SELECT timewindow, quantities FROM real_time_analytics" +
              " WHERE series = ?" +
              " AND timewindow >= ?" +
              " AND   timewindow <= ?" +
              " ORDER BY timewindow DESC LIMIT 60";

      timeslice_query = getSession().prepare(statement);
    }


    BoundStatement boundStatement = timeslice_query.bind(series, start_time, end_time);

      return getSession().execute(boundStatement);
  }


}



