package retail.helpers.jsonoutput;

import com.datastax.driver.core.ResultSet;
import junit.framework.TestCase;
import retail.model.AdHocDAO;

import java.math.BigDecimal;

/**
 * DataStax Academy Sample Application
 * <p/>
 * Copyright 2015 DataStax
 */
public class GoogleJsonArrayViewTest extends TestCase {

    public void testGet_google_type() throws Exception {
        assertEquals("number", GoogleJsonUtils.get_google_type(int.class));
        assertEquals("number", GoogleJsonUtils.get_google_type(BigDecimal.class));
    }

    public void testGetAdHocQueryWithParamters() throws Exception {

        ResultSet resultSet = AdHocDAO.getAdHocQuery(
                "select product_id, release_date, title, supplier_id from products_by_category_name where category_name = ? limit 2",
                "LED TVs");

        String json = GoogleJsonArrayView.toGoogleVisualizationJsonArray(resultSet, null);
        assertEquals("[[{\"id\":\"product_id\",\"label\":\"Product Id\",\"type\":\"string\"},{\"id\":\"release_date\",\"label\":\"Release Date\",\"type\":\"datetime\"},{\"id\":\"title\",\"label\":\"Title\",\"type\":\"string\"},{\"id\":\"supplier_id\",\"label\":\"Supplier Id\",\"type\":\"number\"}],[\"24LB451B\",\"Date(2014,10,20,3,0,0)\",\"LG 24LB451B LED TV\",\"293\"],[\"26LS359S\",\"Date(2012,4,22,3,0,0)\",\"LG 26LS359S LED TV\",\"293\"]]",json);
    }
}