package retail.model;

import junit.framework.TestCase;
import retail.helpers.cassandra.CassandraData;

import java.util.List;

/**
 * DataStax Academy Sample Application
 *
 * Copyright 2013 DataStax
 *
 */

public class ProductDAOTest extends TestCase {

  public void testGetProductsByBrand() throws Exception {
      List<ProductDAO> productDAOList = ProductDAO.getProductsByBrand("1");
      assertEquals(300,productDAOList.size());
  }

  public void testGetProductsByBrandEmpty() throws Exception {
      List<ProductDAO> productDAOList = ProductDAO.getProductsByBrand("");
      assertEquals(0,productDAOList.size());
  }

  public void testGetProductsByCategoryName() throws Exception {
    List<ProductDAO> productDAOList = ProductDAO.getProductsByCategoryName("printer drums");
    assertEquals(19,productDAOList.size());
  }

  public void testGetProductsByCategoryNameEmpty() throws Exception {
    List<ProductDAO> productDAOList = ProductDAO.getProductsByCategoryName("");
      assertEquals(0,productDAOList.size());
  }

    public void testGetProductsSolrQuery() throws Exception {
        String solr_query = CassandraData.makeSolrQueryString("usb", null);
        List<ProductDAO> productDAOList = ProductDAO.getProductsSolrQuery(solr_query);
        assertEquals(138, productDAOList.size());
    }

    public void testMakeSolrQueryString() throws Exception {
        String solrQueryString = CassandraData.makeSolrQueryString("this", "that");
        assertEquals ("\"q\":\"title:this\",\"fq\":\"that\"", solrQueryString);
    }

    public void testMakeSolrQueryStringNoFilter() throws Exception {
        String solrQueryString = CassandraData.makeSolrQueryString("this", null);
        assertEquals ("\"q\":\"title:this\"", solrQueryString);
    }

    public void testMakeSolrQueryStringEscapesQuotes() throws Exception {
        String solrQueryString = CassandraData.makeSolrQueryString("\"this\"", null);
        assertEquals ("\"q\":\"title:\\\"this\\\"\"", solrQueryString);
    }

    public void testGetProductById() throws Exception {
        ProductDAO product = ProductDAO.getProductById("90-IG1Y002M00-0PA0");
        assertEquals("ASUS USB-N53", product.getTitle());
    }

    public void testGetProductByInvalidId() throws Exception {
        ProductDAO product = ProductDAO.getProductById("XXXXX");
        assertNull(product);
    }
}
