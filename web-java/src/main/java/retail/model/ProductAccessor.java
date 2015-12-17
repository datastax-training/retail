package retail.model;

import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;
import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Param;
import com.datastax.driver.mapping.annotations.Query;

import java.util.List;

/**
 * DataStax Academy Sample Application
 * <p/>
 * Copyright 2015 DataStax
 */

@Accessor
public interface ProductAccessor {

    @Query("SELECT * FROM products_by_category_name WHERE category_name = :category_name")
    Result<ProductDAO> getProductsByCategoryName(@Param("category_name") String category_name);

    @Query("SELECT * from products_by_supplier WHERE supplier_id = :brand_id limit 300")
    Result<ProductDAO> getProductsByBrand(@Param("brand_id") int brand_id);

    @Query("SELECT * FROM products_by_id WHERE solr_query = :solr_query LIMIT 300")
    Result<ProductDAO> getProductsSolrQuery(@Param("solr_query") String solr_query);

}
