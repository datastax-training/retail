package retail.model;

import com.datastax.driver.core.*;
import retail.helpers.cassandra.CassandraData;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * DataStax Academy Sample Application
 *
 * Copyright 2013 DataStax
 *
 */


public class ProductDAO extends CassandraData {

  private static PreparedStatement get_product_by_brand_cc = null;
  private static PreparedStatement get_product_by_category_cc = null;
  private static PreparedStatement get_product_by_id_cc = null;

  // Note the CamelCase as jinja2 requires it.
  // Also - only use the boxed types for loadBeanFromRow

  private String productId;
  private Integer categoryId;
  private String categoryName;
  private Map<String,String > features;
  private Boolean isHot;
  private String longDescription;
  private BigDecimal price;
  private Date releaseDate;
  private String shortDescription;
  private Integer supplierId;
  private String supplierName;
  private String  title;
  private String  url;

  public ProductDAO(Row row) {

    // This constructor loads all of the fields of the row using the
    // superclass method loadBeanFromRow.

    // It replaces stuff like
    //    productId = row.getString("product_id");
    //    categoryId = row.getInt("category_id");


    loadBeanFromRow(row);
  }

  public static ProductDAO getProductById(String productId) {

    if (get_product_by_id_cc == null) {
      get_product_by_id_cc = getSession().prepare("SELECT * from products_by_id WHERE product_id = ?");
    }

    BoundStatement boundStatement = get_product_by_id_cc.bind(productId);
    ResultSet resultSet = getSession().execute(boundStatement);

    // Return null if it's not found
    if (resultSet.isExhausted()) {
      return null;
    }

    // Return the first row
    return new ProductDAO(resultSet.one());

  }

  public static List<ProductDAO> getProductsByBrand(String brand_id) {

    if (get_product_by_brand_cc == null) {
      get_product_by_brand_cc = getSession().prepare("SELECT * from products_by_supplier WHERE supplier_id = ? limit 300");
    }

    Statement stmt = null;
    if (brand_id != null && !brand_id.isEmpty()) {
      stmt = get_product_by_brand_cc.bind(Integer.parseInt(brand_id));
    }

    return getProductsWithStmt(stmt);
  }

  public static List<ProductDAO> getProductsByCategoryName(String category_name) {

    if (get_product_by_category_cc == null) {
      get_product_by_category_cc = getSession().prepare("SELECT * from products_by_category_name WHERE category_name = ? limit 300");
    }

    Statement stmt = null;
    if (category_name != null && !category_name.isEmpty()) {
      stmt = get_product_by_category_cc.bind(category_name);
    }

    return getProductsWithStmt(stmt);
  }

  private static List<ProductDAO> getProductsWithStmt(Statement statement) {

    ResultSet results = null;
    int initial_result_size = 0;

    if (statement != null) {
      results = getSession().execute(statement);
      initial_result_size = results.getAvailableWithoutFetching();
    }

    final List<ProductDAO> productDAOList = new ArrayList<ProductDAO>(initial_result_size);

    if (results != null) {
      for (Row row: results) {
        productDAOList.add(new ProductDAO(row));
      }
    }

    return productDAOList;
  }

  public static List<ProductDAO> getProductsSolrQuery(String solr_query) {


    Statement statement = new SimpleStatement("SELECT * FROM products_by_id WHERE solr_query = '{" +
           solr_query + "}' LIMIT 300");

    return getProductsWithStmt(statement);
  }


  public String getProductId() {
    return productId;
  }

  public void setProductId(String productId) {
    this.productId = productId;
  }

  public Integer getCategoryId() {
    return categoryId;
  }

  public void setCategoryId(Integer categoryId) {
    this.categoryId = categoryId;
  }

  public String getCategoryName() {
    return categoryName;
  }

  public void setCategoryName(String categoryName) {
    this.categoryName = categoryName;
  }

  public Map<String, String> getFeatures() {
    return features;
  }

  public void setFeatures(Map<String, String> features) {
    this.features = features;
  }

  public Boolean getIsHot() {
    return isHot;
  }

  public void setIsHot(Boolean isHot) {
    this.isHot = isHot;
  }

  public String getLongDescription() {
    return longDescription;
  }

  public void setLongDescription(String longDescription) {
    this.longDescription = longDescription;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public void setPrice(BigDecimal price) {
    this.price = price;
  }

  public Date getReleaseDate() {
    return releaseDate;
  }

  public void setReleaseDate(Date releaseDate) {
    this.releaseDate = releaseDate;
  }

  public String getShortDescription() {
    return shortDescription;
  }

  public void setShortDescription(String shortDescription) {
    this.shortDescription = shortDescription;
  }

  public Integer getSupplierId() {
    return supplierId;
  }

  public void setSupplierId(Integer supplierId) {
    this.supplierId = supplierId;
  }

  public String getSupplierName() {
    return supplierName;
  }

  public void setSupplierName(String supplierName) {
    this.supplierName = supplierName;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }
}
