package retail.controller;

import com.google.common.collect.Maps;
import retail.helpers.jinjahelper.JinjaHelper;
import retail.helpers.jinjahelper.JinjaServlet;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * DataStax Academy Sample Application
 *
 * Copyright 2013 DataStax
 *
 */


// URI format /gcharts/<chart type>?q=query&options=<chart options>& any other options for the query
public class GoogleChartServlet extends JinjaServlet {

    // Map the chart type to the correct google package
    private static final Map<String,String> supported_charts = new HashMap<>();
    static {
        supported_charts.put("Table","table");
        supported_charts.put("GeoChart","geochart");
        supported_charts.put("BarChart","corechart");
        supported_charts.put("ColumnChart","corechart");
        supported_charts.put("LineChart","corechart");
        supported_charts.put("PieChart","corechart");
        supported_charts.put("AreaChart","corechart");
    }


    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ServletOutputStream out = response.getOutputStream();
        Map<String, Object> context = Maps.newHashMap();

        // This posts the result set in the google format

        // remove the servlet path from the the URI, and we get the chart type
        String chart_type = request.getRequestURI()
                .replaceFirst(request.getServletPath(),"")
                .replace("/","");

        String options = request.getParameter("options");
        String url = request.getParameter("url");

        ArrayList<String> url_parms = new ArrayList<>();
        for (String param_key: request.getParameterMap().keySet()) {
            if (!param_key.contains("url") && !param_key.contains("options")) {
                url_parms.add(param_key);
                url_parms.add(request.getParameter(param_key));
            }
        }

        String ajax_source = JinjaHelper.makeURL(url,url_parms.toArray(new String[url_parms.size()]));

        if (options == null || options.isEmpty()) {
            options = "{}";
        }

        context.put("ajax_source", ajax_source);
        context.put("options", options);
        context.put("chart_type", chart_type);
        context.put("package", supported_charts.get(chart_type));

        byte[] renderedTemplate = render("/google_charts.jinja2", context);
        out.write(renderedTemplate);
    }
}
