package playlist.model;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DataStax Academy Sample Application
 *
 * Copyright 2013 DataStax
 *
 */


public class FacetDAO extends CassandraData {

    private String name;
    private Long quantity;

    public FacetDAO(String name, Long quantity) {
        this.name = name;
        this.quantity = quantity;
    }

    public static Map<String, List<FacetDAO>> getSolrQueryFacets(String solr_query, String ... facet_columns) {
        final Map<String, List<FacetDAO>> facetMap = new HashMap<>();

        StringBuilder statement = new StringBuilder()
                .append("SELECT * FROM retail.products_by_id WHERE solr_query = '{")
                .append(solr_query)
                .append(",\"facet\":{\"field\":[");

        String comma = "";
        for (String column: facet_columns) {
            statement.append(comma)
                    .append("\"")
                    .append(column)
                    .append("\"");
            comma=",";
        }
        statement.append("]}}'");

        ResultSet resultSet = getSession().execute(statement.toString());

        String facet_json = resultSet.one().getString("facet_fields");

        JSONObject jsonObj = (JSONObject)JSONValue.parse(facet_json);
        for (Object field: jsonObj.keySet()) {

            JSONObject facets = (JSONObject)jsonObj.get(field);

            List<FacetDAO> facetList = new ArrayList<>();

            for (Object entry: facets.entrySet()) {
                Map.Entry<String,Long> j = (Map.Entry<String,Long>) entry;
                if (j.getValue() > 0) {
                    facetList.add(new FacetDAO(j.getKey(), j.getValue()));
                }
            }
            facetMap.put((String) field, facetList);
        }

        return facetMap;
    }

}