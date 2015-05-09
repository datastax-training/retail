package retail.controller;

import com.datastax.driver.core.ResultSet;
import retail.helpers.jsonoutput.GoogleJsonArrayView;
import retail.model.AdHocDAO;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * DataStax Academy Sample Application
 *
 * Copyright 2013 DataStax
 *
 */

public class SimpleQueryServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        // This posts the result set in the google format

        String query = request.getParameter("q");
        String order_col = request.getParameter("order_col");

        ResultSet resultset = AdHocDAO.getAdHocQuery(query);

        // return a json array (list of lists)
        // Build the header json

        String json = GoogleJsonArrayView.toGoogleVisualizationJsonArray(resultset, order_col);

        out.print(json);
    }
}
