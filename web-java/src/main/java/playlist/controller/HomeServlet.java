package playlist.controller;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import playlist.model.TimeSeriesDAO;
import playlist.model.StatisticsDAO;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Map;

/**
 * DataStax Academy Sample Application
 *
 * Copyright 2013 DataStax
 *
 */

public class HomeServlet extends JinjaServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();

        Map<String, Object> context = Maps.newHashMap();
        context.put("name", "Jared");

        String renderedTemplate = render("/index.jinja2", context);
        out.println(renderedTemplate);
    }
}
