package retail.model;

import com.datastax.driver.core.*;
import retail.helpers.cassandra.CassandraData;

import java.util.HashMap;
import java.util.Map;

/**
 * DataStax Academy Sample Application
 *
 * Copyright 2013 DataStax
 *
 */

public class AdHocDAO extends CassandraData {

    public static final Map<String, PreparedStatement> preparedStatements =
            new HashMap<>();


    // Return the results of the given query in a google array format
    // for compatability with google maps
    //
    // [
    //    [{"id": colname, "label": nice name, "type": google type},... ]
    //     [ column value, column value, ...],
    //     ...
    //  ]
    //
    public static ResultSet getAdHocQuery (String query, Object ... parameters) {

        PreparedStatement preparedStatement = preparedStatements.get(query);
        if (preparedStatement == null) {
            preparedStatement = getSession().prepare(query);
            preparedStatements.put(query, preparedStatement);
        }

        BoundStatement boundStatement = preparedStatement.bind(parameters);

        return getSession().execute(boundStatement);
    }

}
