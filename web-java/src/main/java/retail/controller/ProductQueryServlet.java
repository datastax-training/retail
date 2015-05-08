package retail.controller;

import com.google.common.collect.Maps;
import retail.helpers.jinjahelper.JinjaServlet;
import retail.model.ProductDAO;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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
    ServletOutputStream out = response.getOutputStream();
    Map<String, Object> context = Maps.newHashMap();

    String brand_id = request.getParameter("brand_id");
    String category_name = request.getParameter("category_name");

    List<ProductDAO> products = null;

    if (brand_id != null && !brand_id.isEmpty()) {
      products = ProductDAO.getProductsByBrand(brand_id);
    } else if (category_name != null && !category_name.isEmpty()) {
      products = ProductDAO.getProductsByCategoryName(category_name);
    }

    context.put("products", products);

    byte[] renderedTemplate = render("/product_list.jinja2", context);
    out.write(renderedTemplate);

  }
}
