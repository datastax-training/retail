package playlist.model;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DataStax Academy Sample Application
 *
 * Copyright 2013 DataStax
 *
 */


public class FacetDAO extends CassandraData {

    private static PreparedStatement facet_query = null;

    private String  title;
    private String  product_id;

    private static Map<String, FacetDAO> getSolrQueryFacets(String q, String fq, String ... facet_columns) {
        final Map<String, FacetDAO> facetMap = null;


        return facetMap;
    }

}