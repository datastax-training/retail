package retail.helpers.jsonoutput;

import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Comparator;

/**
 * DataStax Academy Sample Application
 * <p/>
 * Copyright 2015 DataStax
 */
public class GoogleJsonArrayView {



    public static String toGoogleVisualizationJsonArray(ResultSet resultset, String sort_field) {

        // Create an array row of column definitions
        // [{"id":<name>, "label":<nice name>, "type":<type>}, ... ]

        JSONArray definitions = new JSONArray();
        ColumnDefinitions columnDefinitions = resultset.getColumnDefinitions();
        for (ColumnDefinitions.Definition coldef: columnDefinitions) {
            JSONObject jsonColDef = GoogleJsonUtils.getJsonColumnDef(coldef.getName(), coldef.getType().asJavaClass());
            definitions.add(jsonColDef);
        }

        JSONArray resultsArray = new JSONArray();
        for (Row row: resultset) {
            JSONArray resultRowArray = new JSONArray();
            for (int i = 0; i < columnDefinitions.size(); i++) {
                Object value = GoogleJsonUtils.google_column_to_object(row, i);
                resultRowArray.add(value);
            }
            resultsArray.add(resultRowArray);
        }

        // Sort by the sort field
        if (sort_field != null && !sort_field.isEmpty()) {
            String[] sort_field_parts = sort_field.split(" ");

            boolean reverse = sort_field_parts.length > 1
                    && sort_field_parts[1].equalsIgnoreCase("desc");

            final int sort_index = columnDefinitions.getIndexOf(sort_field_parts[0]);
            sortResultsArray(resultsArray, sort_index, reverse);
        }

        // Build the result array
        JSONArray outputArray = new JSONArray();
        outputArray.add(definitions);
        outputArray.addAll(resultsArray);
        return outputArray.toJSONString();
    }

    private static void sortResultsArray(JSONArray resultsArray, final int sort_index, final boolean reverse) {
        resultsArray.sort(new Comparator<JSONArray>() {
            @Override
            public int compare(JSONArray o1, JSONArray o2) {
                return (reverse?-1:1) * ((Comparable)o1.get(sort_index)).compareTo(o2.get(sort_index));
            }
        });
    }

}
