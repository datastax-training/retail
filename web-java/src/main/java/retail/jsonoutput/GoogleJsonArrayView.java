package retail.jsonoutput;

import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import org.apache.commons.lang3.text.WordUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import retail.model.AdHocDAO;

import java.text.SimpleDateFormat;

/**
 * DataStax Academy Sample Application
 * <p/>
 * Copyright 2015 DataStax
 */
public class GoogleJsonArrayView {

    public static final SimpleDateFormat GoogleDateFormat = new SimpleDateFormat("'Date('y,M,d,H,m,s')'" );

    public static String toGoogleVisualizationJsonArray(ResultSet resultset) {

        JSONArray definitions = new JSONArray();
        ColumnDefinitions columnDefinitions = resultset.getColumnDefinitions();
        for (ColumnDefinitions.Definition coldef: columnDefinitions) {
            JSONObject jsonColDef = new JSONObject();
            jsonColDef.put("id", coldef.getName());
            jsonColDef.put("label", column_name_to_label(coldef.getName()));
            jsonColDef.put("type", get_google_type(coldef.getType().asJavaClass()));
            definitions.add(jsonColDef);
        }

        JSONArray resultsArray = new JSONArray();
        for (Row row: resultset) {
            JSONArray resultRowArray = new JSONArray();
            for (int i = 0; i < columnDefinitions.size(); i++) {
                String value = column_to_string(row, i, columnDefinitions.getType(i).asJavaClass());
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

    public static String get_google_type (Class cassandra_clazz) {

        String type_name = cassandra_clazz.getName();
        switch (type_name) {
            case "java.lang.Integer":
            case "java.lang.Double":
            case "java.lang.Float":
            case "java.lang.Long":
            case "int":
            case "double":
            case "float":
            case "long":
            case "java.math.BigDecimal":
            case "java.math.BigInteger":
                return "number";
            case "java.lang.Boolean":
            case "boolean":
                return "boolean";
            case "java.util.Date":
                return "datetime";
            case "java.lang.String":
                return "string";
            case "java.util.UUID":
                return "string";
            default:
                throw new UnsupportedOperationException(type_name + "is not supported.");
        }
    }

    public static String column_to_string (Row row, int index, Class cassandra_clazz) {

        String type_name = cassandra_clazz.getName();
        if (row.isNull(index)) {
            return "";
        }

        switch (type_name) {
            case "java.lang.Integer":
                return Integer.toString(row.getInt(index));
            case "java.lang.Double":
                return Double.toString(row.getDouble(index));
            case "java.lang.float":
                return Float.toString(row.getFloat(index));
            case "java.lang.Long":
                return Long.toString(row.getLong(index));
            case "java.math.BigDecimal":
                return row.getDecimal(index).toString();
            case "java.math.BigInteger":
                return row.getVarint(index).toString();
            case "java.lang.Boolean":
                return Boolean.toString(row.getBool(index));
            case "java.util.Date":
                return GoogleDateFormat.format(row.getDate(index));
            case "java.lang.String":
                return row.getString(index);
            case "java.util.UUID":
                return row.getUUID(index).toString();
            default:
                throw new UnsupportedOperationException(type_name + "is not supported.");
        }
    }



    private static String column_name_to_label(String column_name) {
        String string1 = column_name.replace('_',' ');
        return WordUtils.capitalize(string1);
    }
}
