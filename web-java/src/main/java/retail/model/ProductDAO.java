package retail.model;

import com.datastax.driver.core.*;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.*;
import retail.helpers.cassandra.CassandraData;
import retail.model.ProductAccessor;

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


@Table(name = "products_by_id",
        readConsistency = "LOCAL_ONE",
        writeConsistency = "LOCAL_QUORUM")

public class ProductDAO extends CassandraData {

  private static PreparedStatement get_product_by_brand_cc = null;
  private static Mapper<ProductDAO> mapper = getMappingManager(ProductDAO.class);
  private static ProductAccessor accessor = mapper.getManager().createAccessor(ProductAccessor.class);

  // Note the CamelCase as jinja2 requires it.
  // Also - only use the boxed types for loadBeanFromRow

  @PartitionKey
  @Column(name = "product_id")
  private String productId;
  @Column(name = "category_id")
  private Integer categoryId;
  @Column(name = "category_name")
  private String categoryName;
  @Column
  private Map<String,String > features;
  @Column(name = "is_hot")
  private Boolean isHot;
  @Column(name = "long_description")
  private String longDescription;
  @Column
  private BigDecimal price;
  @Column(name = "release_date")
  private Date releaseDate;
  @Column(name = "short_description")
  private String shortDescription;
  @Column(name = "supplier_id")
  private Integer supplierId;
  @Column(name = "supplier_name")
  private String supplierName;
  @Column
  private String title;
  @Column
  private String url;

  public static ProductDAO getProductById(String productId) {
    // Return the first row
    return mapper.get(productId);
  }

  public static List<ProductDAO> getProductsByBrand(String brand_id) {
     return accessor.getProductsByBrand(Integer.parseInt(brand_id)).all();
  }

  public static List<ProductDAO> getProductsByCategoryName(String category_name) {
     return accessor.getProductsByCategoryName(category_name).all();
  }

  public static List<ProductDAO> getProductsSolrQuery(String solr_query) {
    return accessor.getProductsSolrQuery("{" + solr_query + "}").all();
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
