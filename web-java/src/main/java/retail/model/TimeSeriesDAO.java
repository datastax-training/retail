package retail.model;

import com.datastax.driver.core.Row;

/**
 * DataStax Academy Sample Application
 *
 * Copyright 2013 DataStax
 *
 * This class returns various data about the Cassandra cluster to which the application is connected
 *
 */

public class TimeSeriesDAO extends CassandraData {

  private final String clusterName;
  private final String cassandraVersion;

  /**
   * Constructor to query cassandra for the release_version and cluster_name
   */

  public TimeSeriesDAO() {

    Row row = getSession().execute("select cluster_name, release_version from system.local").one();
    cassandraVersion = row.getString("release_version");
    clusterName = row.getString("cluster_name");

  }

  public String getClusterName() {
    return clusterName;
  }

  public String getCassandraVersion() {
    return cassandraVersion;
  }
}
