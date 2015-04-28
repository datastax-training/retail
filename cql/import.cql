
-- Suppliers
copy retail.suppliers (supplier_id, supplier_name) from '../csv/suppliers.csv' with delimiter = '|' AND header = false ;

-- Products
copy retail.products_by_id (supplier_name, supplier_id, title, product_id, release_date, category_name, category_id, long_description, short_description, url, features ) from '../csv/products.csv' with delimiter = '	' AND header = true ;
copy retail.products_by_supplier (supplier_name, supplier_id, title, product_id, release_date, category_name, category_id, long_description, short_description, url, features ) from '../csv/products.csv' with delimiter = '	' AND header = true ;
copy retail.products_by_category_name (supplier_name, supplier_id, title, product_id, release_date, category_name, category_id, long_description, short_description, url, features ) from '../csv/products.csv' with delimiter = '	' AND header = true ;

-- Stores
copy retail.stores (store_id,address,address_2,address_3,city,state,zip,size_in_sf) FROM '../csv/stores.csv' with delimiter = '|' and header=true;
