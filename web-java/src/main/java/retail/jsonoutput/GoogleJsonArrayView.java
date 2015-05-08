package retail.jsonoutput;

import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * DataStax Academy Sample Application
 * <p/>
 * Copyright 2015 DataStax
 */
public class GoogleJsonArrayView {



    public static String toGoogleVisualizationJsonArray(ResultSet resultset) {

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

 //       googleResults.sort()

        // Build the result array
        JSONArray outputArray = new JSONArray();
        outputArray.add(definitions);
        outputArray.addAll(resultsArray);
        return outputArray.toJSONString();
    }

}
