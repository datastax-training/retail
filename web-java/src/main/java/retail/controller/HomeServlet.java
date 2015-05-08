package retail.controller;

import com.google.common.collect.Maps;
import retail.helpers.jinjahelper.JinjaServlet;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * DataStax Academy Sample Application
 *
 * Copyright 2013 DataStax
 *
 */

public class HomeServlet extends JinjaServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ServletOutputStream out = response.getOutputStream();

        Map<String, Object> context = Maps.newHashMap();
        context.put("name", "Jared");

        byte[] renderedTemplate = render("/index.jinja2", context);
        out.write(renderedTemplate);
    }
}
