package retail.controller;

import com.google.common.collect.Maps;
import retail.jinjahelper.JinjaServlet;
import retail.model.ProductDAO;

import javax.servlet.ServletException;
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

public class ProductServlet extends JinjaServlet {

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    PrintWriter out = response.getWriter();
    Map<String, Object> context = Maps.newHashMap();

    String product_id = request.getParameter("product_id");

    List<ProductDAO> products = null;

    ProductDAO product = ProductDAO.getProductById(product_id);

    context.put("products", products);

    String renderedTemplate = render("/product_list.jinja2", context);
    out.println(renderedTemplate);

  }
}
