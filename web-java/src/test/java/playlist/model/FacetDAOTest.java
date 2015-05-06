package playlist.model;

import junit.framework.TestCase;

/**
 * DataStax Academy Sample Application
 * <p/>
 * Copyright 2015 DataStax
 */
public class FacetDAOTest extends TestCase {

    public void testGetSolrQueryFacets() throws Exception {
        FacetDAO.getSolrQueryFacets("\"q\":\"title:usb\"","category_name");

    }

    public void testGetSolrQuery2Facets() throws Exception {
        FacetDAO.getSolrQueryFacets("\"q\":\"title:usb\"","category_name","supplier_name");
    }
}