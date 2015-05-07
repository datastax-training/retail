package retail.model;

import junit.framework.TestCase;

import java.util.List;
import java.util.Map;

/**
 * DataStax Academy Sample Application
 * <p/>
 * Copyright 2015 DataStax
 */
public class FacetDAOTest extends TestCase {

    public void testGetSolrQueryFacets() throws Exception {
        Map<String, List<FacetDAO>> facets = FacetDAO.getSolrQueryFacets("\"q\":\"title:usb\"", "category_name");
        assertEquals(1, facets.size());
        // TODO add more conditions here
    }

    public void testGetSolrQuery2Facets() throws Exception {
        Map<String, List<FacetDAO>> facets = FacetDAO.getSolrQueryFacets("\"q\":\"title:usb\"","category_name","supplier_name");
        assertEquals(2, facets.size());
        // TODO add more conditions here
    }
}