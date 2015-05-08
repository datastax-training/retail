package retail.controller;

import com.google.common.collect.Maps;
import retail.helpers.jinjahelper.JinjaServlet;
import retail.helpers.cassandra.CassandraData;
import retail.model.FacetDAO;
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

public class ProductSolrServlet extends JinjaServlet {

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    ServletOutputStream out = response.getOutputStream();
    Map<String, Object> context = Maps.newHashMap();

    String search_term = request.getParameter("s");
    String filter_by = request.getParameter("filter_by");

    String solr_query = CassandraData.makeSolrQueryString(search_term, filter_by);

    List<ProductDAO> products = ProductDAO.getProductsSolrQuery(solr_query);

    Map<String, List<FacetDAO>> facetsMap = FacetDAO.getSolrQueryFacets(solr_query, "category_name", "supplier_name");


    context.put("search_term", search_term);
    context.put("products", products);
    context.put("categories", facetsMap.get("category_name"));
    context.put("suppliers", facetsMap.get("supplier_name"));

    byte[] renderedTemplate = render("/search_list.jinja2", context);
    out.write(renderedTemplate);

  }
}
