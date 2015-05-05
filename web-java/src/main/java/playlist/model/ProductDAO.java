package playlist.model;

import com.datastax.driver.core.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * DataStax Academy Sample Application
 *
 * Copyright 2013 DataStax
 *
 */


public class ProductDAO extends CassandraData {

  private static PreparedStatement get_product_by_brand_cc = null;
  private static PreparedStatement get_product_by_category_cc = null;

  private String  title;
  private String product_id;
  private String  category_name;
  private Integer category_id;
  private String  supplier_name;
  private Integer  supplier_id;

  public ProductDAO() {
    title = null;
    product_id = null;
    category_name = null;
    category_id = null;
    supplier_name = null;
    supplier_id = null;
  }

  public ProductDAO(Row row) {
    title = row.getString("title");
    product_id = row.getString("product_id");
    category_name = row.getString("category_name");
    category_id = row.getInt("category_id");
    supplier_name = row.getString("supplier_name");
    supplier_id = row.getInt("supplier_id");
  }

  public static List<ProductDAO> getProductsByBrand(String brand_id) {

    if (get_product_by_brand_cc == null) {
      get_product_by_brand_cc = getSession().prepare("SELECT * from retail.products_by_supplier WHERE supplier_id = ? limit 300");
    }

    Statement stmt = null;
    if (brand_id != null && !brand_id.isEmpty()) {
      stmt = get_product_by_brand_cc.bind(Integer.parseInt(brand_id));
    }

    return getProductsWithStmt(stmt);
  }

  public static List<ProductDAO> getProductsByCategoryName(String category_name) {

    if (get_product_by_category_cc == null) {
      get_product_by_category_cc = getSession().prepare("SELECT * from retail.products_by_category_name WHERE category_name = ? limit 300");
    }

    Statement stmt = null;
    if (category_name != null && !category_name.isEmpty()) {
      stmt = get_product_by_category_cc.bind(category_name);
    }

    return getProductsWithStmt(stmt);
  }

  private static List<ProductDAO> getProductsWithStmt(Statement statement) {
    final List<ProductDAO> productDAOList = new ArrayList<ProductDAO>();

    ResultSet results = null;

    if (statement != null) {
      results = getSession().execute(statement);
    }

    if (results != null) {
      for (Row row: results) {
        productDAOList.add(new ProductDAO(row));
      }
    }

    return productDAOList;
  }

  public static List<ProductDAO> getProductsSolrQuery(String search_term, String filter_by) {

    String solr_query = makeSolrQueryString(search_term, filter_by);

    Statement statement = new SimpleStatement("SELECT * FROM retail.products_by_id WHERE solr_query = '{" +
           solr_query + "}' LIMIT 300");

    return getProductsWithStmt(statement);
  }

  public static String makeSolrQueryString(String search_term, String filter_by) {
    String solr_query = "\"q\":\"title:"+search_term +"\"";

    if (filter_by != null && !filter_by.isEmpty()) {
      solr_query += "\"fq\":\""+filter_by+"\"";
    }
    return solr_query;
  }


  public String getTitle() {
    return title;
  }

  public String getProductId() {
    return product_id;
  }

  public String getCategoryName() {
    return category_name;
  }

  public Integer getCategoryId() {
    return category_id;
  }

  public String getSupplierName() {
    return supplier_name;
  }

  public Integer getSupplierId() {
    return supplier_id;
  }

}
