package retail.jsonoutput;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import org.json.simple.JSONArray;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * DataStax Academy Sample Application
 * <p/>
 * Copyright 2015 DataStax
 */
public class GoogleJsonTimesliceView {
    public static String getGoogleJsonTimesliceView(ResultSet resultSet) {
      JSONArray description = new JSONArray();
      description.add(GoogleJsonUtils.getJsonColumnDef("time_window", Date.class));

      // Turn the map into result columns and data
      JSONArray resultsArray = new JSONArray();
      if (!resultSet.isExhausted()) {
        Set<String> first_row_keys = null;
        for (Row row: resultSet) {
          JSONArray jsonRow = new JSONArray();

          // Save the first row to build the description
          if (first_row_keys == null) {
            first_row_keys = row.getMap("quantities", String.class, Integer.class).keySet();
          }
          // add the time window
          jsonRow.add(GoogleJsonUtils.column_to_object(row,0));

          // Iterate through the map and make the elements look like columns
          Map<String, Integer> quantityMap = row.getMap("quantities", String.class, Integer.class);
          for (String key: first_row_keys) {
            jsonRow.add(quantityMap.get(key));
          }
          resultsArray.add(jsonRow);
        }

        // Build the rest of the description off the first row

        if (first_row_keys != null) {
          for (String key: first_row_keys) {
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
