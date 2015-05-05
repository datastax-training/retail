package playlist.model;

import com.datastax.driver.core.Session;
import junit.framework.TestCase;

import java.util.List;

/**
 * DataStax Academy Sample Application
 *
 * Copyright 2013 DataStax
 *
 */

public class StatisticsDAOTest extends TestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    Session session  = CassandraData.getSession();
    session.execute("TRUNCATE statistics");

  }


  public void testIncrement_counter() throws Exception {
      StatisticsDAO.increment_counter("TestCounter");
      List<StatisticsDAO> stats = StatisticsDAO.getStatistics();
      assertEquals(1, stats.get(0).getCounter_value());

    StatisticsDAO.increment_counter("TestCounter");
    stats = StatisticsDAO.getStatistics();
    assertEquals(2, stats.get(0).getCounter_value());

  }

  public void testDecrement_counter() throws Exception {
    StatisticsDAO.increment_counter("TestCounter1");
    List<StatisticsDAO> stats = StatisticsDAO.getStatistics();
    assertEquals(1, stats.get(0).getCounter_value());

    StatisticsDAO.decrement_counter("TestCounter1");
    stats = StatisticsDAO.getStatistics();
    assertEquals(0, stats.get(0).getCounter_value());
  }

}
