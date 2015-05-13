package retail.model;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import junit.framework.TestCase;
import retail.helpers.jsonoutput.GoogleJsonArrayView;

import java.util.List;

/**
 * DataStax Academy Sample Application
 * <p/>
 * Copyright 2015 DataStax
 */
public class AdHocDAOTest extends TestCase {

    public void testGetAdHocQuery() throws Exception {

        ResultSet resultSet = AdHocDAO.getAdHocQuery("select * from suppliers");
        List<Row> resultSetList = resultSet.all();
        assertEquals(419, resultSetList.size());
    }

    public void testGetAdHocQueryWithParamters() throws Exception {

        ResultSet resultSet = AdHocDAO.getAdHocQuery(
                "select * from products_by_category_name where category_name = ?",
                "LED TVs");

        List<Row> resultSetList = resultSet.all();
        assertEquals(509, resultSetList.size());

    }
}