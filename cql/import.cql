
-- Suppliers
COPY retail.suppliers (supplier_id, supplier_name) FROM '../csv/suppliers.csv' WITH delimiter = '|' AND header = false ;

-- Products
COPY retail.products_by_id (supplier_name, supplier_id, title, product_id, release_date, category_name, category_id, long_description, short_description, url, features ) from '../csv/products.csv' with delimiter = '\t' AND header = true ;
COPY retail.products_by_supplier (supplier_name, supplier_id, title, product_id, release_date, category_name, category_id, long_description, short_description, url, features ) from '../csv/products.csv' with delimiter = '\t' AND header = true ;
COPY retail.products_by_category_name (supplier_name, supplier_id, title, product_id, release_date, category_name, category_id, long_description, short_description, url, features ) from '../csv/products.csv' with delimiter = '\t' AND header = true ;

-- Stores
COPY retail.stores (store_id,address,address_2,address_3,city,state,zip,size_in_sf) FROM '../csv/stores.csv' WITH delimiter = '|' AND header=true;

-- Make some hot products
UPDATE retail.products_by_category_name SET is_hot = true WHERE category_name = 'notebooks';
UPDATE retail.products_by_category_name SET is_hot = true WHERE category_name = 'servers';

