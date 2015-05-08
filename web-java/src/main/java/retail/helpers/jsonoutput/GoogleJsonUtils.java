package retail.helpers.jsonoutput;

import com.datastax.driver.core.Row;
import org.apache.commons.lang3.text.WordUtils;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import retail.helpers.cassandra.CassandraData;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * DataStax Academy Sample Application
 * <p/>
 * Copyright 2015 DataStax
 */
public class GoogleJsonUtils {

    public static final SimpleDateFormat GoogleDateFormat;
    static {
        GoogleDateFormat = new SimpleDateFormat("'Date('y,M,d,H,m,s')'");
        GoogleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    // Create a Json column definition object
    // {"id":<name>, "label":<nice name>, "type":<type>}

    static public JSONObject getJsonColumnDef(String col_name, Class java_type) {
            JSONObject jsonColDef = new JSONObject();
            jsonColDef.put("id", col_name);
            jsonColDef.put("label", column_name_to_label(col_name));
            jsonColDef.put("type", get_google_type(java_type));
            return jsonColDef;
        }

        private static String column_name_to_label(String column_name) {
            String string1 = column_name.replace('_', ' ');
            return WordUtils.capitalize(string1);
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

        // Return the column value as an object of the appropriate type and format for google charts
        public static Object google_column_to_object (Row row, int index) {

            Class cassandra_clazz = row.getColumnDefinitions().getType(index).asJavaClass();
            String type_name = cassandra_clazz.getName();

            Object data_object = CassandraData.column_to_object(row, index);
            if (data_object == null) {
                return "";
            }
            switch (type_name) {
                case "java.lang.Double":
                    return new JsonNoScientificNotation((Double) data_object);
                case "java.lang.float":
                    return new JsonNoScientificNotation((Double) data_object);
                case "java.math.BigDecimal":
                    return new JsonNoScientificNotation(((BigDecimal)data_object).doubleValue());
                case "java.util.Date":
                    return GoogleDateFormat.format(row.getDate(index));   // treat as string
                default:
                    return data_object;
            }
        }

        // Custom formatter for decimal numbers - only 5 decimal places
        public static class JsonNoScientificNotation implements JSONAware {

            double number;

            public JsonNoScientificNotation(double number) {
                this.number = number;
            }

            public String toJSONString() {
                return new DecimalFormat("#.#####").format(number);
            }
        }
}
