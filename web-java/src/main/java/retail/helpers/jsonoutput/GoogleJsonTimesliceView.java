package retail.helpers.jsonoutput;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import org.json.simple.JSONArray;

import java.math.BigDecimal;
import java.util.*;

/**
 * DataStax Academy Sample Application
 * <p/>
 * Copyright 2015 DataStax
 */
public class GoogleJsonTimesliceView {
    public static String getGoogleJsonTimesliceView(ResultSet resultSet) {
      JSONArray description = new JSONArray();
      description.add(GoogleJsonUtils.getJsonColumnDef("time_window", Date.class));


      // Find all of the products in the results set - those are the map keys
      // Coalesce all of the keys
      // Turn the map into result columns and data
      JSONArray resultsArray = new JSONArray();
      if (!resultSet.isExhausted()) {
        List<Row> resultList = resultSet.all();
        Set<String> row_keys = new HashSet<>();
        for (Row row : resultList) {
          row_keys.addAll(row.getMap("quantities", String.class, Integer.class).keySet());
        }

        for (Row row: resultList) {
          JSONArray jsonRow = new JSONArray();

          // add the time window
          jsonRow.add(GoogleJsonUtils.google_column_to_object(row, 0));

          // Iterate through the map and make the elements look like columns
          Map<String, Integer> quantityMap = row.getMap("quantities", String.class, Integer.class);
          for (String key: row_keys) {
            jsonRow.add(quantityMap.get(key));
          }
          resultsArray.add(jsonRow);
        }

        // Build the rest of the description off the first row

        if (row_keys != null) {
          for (String key: row_keys) {
            description.add(GoogleJsonUtils.getJsonColumnDef(key,Integer.class));
          }
        }

      } else {
        description.add(GoogleJsonUtils.getJsonColumnDef("no_data", BigDecimal.class));
      }
      // Create the final result set
      JSONArray googleResults = new JSONArray();
      googleResults.add(description);
      googleResults.addAll(resultsArray);
      return googleResults.toJSONString();
    }
}
