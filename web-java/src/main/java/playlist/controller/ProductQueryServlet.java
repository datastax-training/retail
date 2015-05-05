package playlist.controller;

import com.google.common.collect.Maps;
import playlist.model.AdHocDAO;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

/**
 * DataStax Academy Sample Application
 *
 * Copyright 2013 DataStax
 *
 */

public class ProductQueryServlet extends JinjaServlet {

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    PrintWriter out = response.getWriter();

    Map<String, Object> context = Maps.newHashMap();
    context.put("name", "Jared");

    String renderedTemplate = render("/product_list.jinja2", context);
    out.println(renderedTemplate);

  }
}
