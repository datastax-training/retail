package retail.controller;

import com.google.common.collect.Maps;
import retail.helpers.jinjahelper.JinjaServlet;
import retail.model.ProductDAO;

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

public class ProductServlet extends JinjaServlet {

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    ServletOutputStream out = response.getOutputStream();
    Map<String, Object> context = Maps.newHashMap();

    String product_id = request.getParameter("product_id");

    if (product_id != null && !product_id.isEmpty()) {
      ProductDAO product = ProductDAO.getProductById(product_id);

      context.put("product", product);
      if (product != null) {
        context.put("features", product.getFeatures());
      }
    }

    byte[] renderedTemplate = render("/product_detail.jinja2", context);
    out.write(renderedTemplate);

  }
}
