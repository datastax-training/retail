package retail.controller;

import com.datastax.driver.core.ResultSet;
import retail.helpers.jsonoutput.GoogleJsonTimesliceView;
import retail.model.TimeSliceDAO;

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

public class TimeSliceQueryServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        // This posts the result set in the google format
        String series = request.getRequestURI()
                .replaceFirst(request.getServletPath(),"")
                .replace("/","");

        Integer minutes = new Integer(request.getParameter("minutes"));

        ResultSet resultset = TimeSliceDAO.getTimeSlice(series, minutes);

        // return a json array (list of lists)
        // Build the header json

        String json = GoogleJsonTimesliceView.getGoogleJsonTimesliceView(resultset);

        out.print(json);
    }
}
