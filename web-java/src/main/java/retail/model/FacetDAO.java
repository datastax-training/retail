package retail.model;

import com.datastax.driver.core.ResultSet;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import retail.helpers.cassandra.CassandraData;

import java.util.*;

/**
 * DataStax Academy Sample Application
 *
 * Copyright 2013 DataStax
 *
 */


public class FacetDAO extends CassandraData {

    private String name;
    private Long amount;



    public FacetDAO(String name, Long amount) {
        this.name = name;
        this.amount = amount;
    }

    public String getName() {
        return name;
    }

    public Long getAmount() {
        return amount;
    }

    public static Map<String, List<FacetDAO>> getSolrQueryFacets(String solr_query, String ... facet_columns) {
        final Map<String, List<FacetDAO>> facetMap = new HashMap<>();

        StringBuilder statement = new StringBuilder()
                .append("SELECT * FROM products_by_id WHERE solr_query = '{")
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
            facetList.sort(new Comparator<FacetDAO>() {
                @Override
                public int compare(FacetDAO o1, FacetDAO o2) {
                    // Note: opposite of normal for reverse sort
                    return -o1.amount.compareTo(o2.amount);
                }
            });
            facetMap.put((String) field, facetList);
        }

        return facetMap;
    }

}