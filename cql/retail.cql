CREATE KEYSPACE IF NOT EXISTS retail
WITH replication = { 'class' : 'NetworkTopologyStrategy', 'DC1' : 1, 'DC2' : 1 };


-- static data

-- Sample Data:
-- product_id: B0009IB8RC
-- brand: null
-- title: Plantronics Headband with Tripod for CS50
-- price: 9.73

CREATE TABLE IF NOT EXISTS retail.suppliers (
   supplier_id int,
   supplier_name text,
   PRIMARY KEY (supplier_id)
);

CREATE TABLE IF NOT EXISTS retail.products_by_id (
   supplier_name text,
   supplier_id int,
   title text,
   product_id text,
   release_date timestamp,
   category_name text,
   category_id int,
   long_description text,
   short_description text,
   url text,
   features map<text,text>,
   price decimal,
   PRIMARY KEY (product_id)
);

CREATE TABLE IF NOT EXISTS retail.products_by_supplier (
   supplier_name text,
   supplier_id int,
   title text,
   product_id text,
   release_date timestamp,
   category_name text,
   category_id int,
   long_description text,
   short_description text,
   url text,
   features map<text,text>,
   price decimal,
   PRIMARY KEY (supplier_id, title, product_id)
);

CREATE TABLE IF NOT EXISTS retail.products_by_category_name (
   supplier_name text,
   supplier_id int,
   title text,
   product_id text,
   release_date timestamp,
   category_name text,
   category_id int,
   long_description text,
   short_description text,
   url text,
   features map<text,text>,
   price decimal,
   is_hot boolean static,
   PRIMARY KEY (category_name, supplier_name, title, product_id)
);

CREATE TABLE IF NOT EXISTS retail.receipts (
       credit_card_type text static,
       credit_card_number bigint static,
       store_id int static,
       register_id int static,
       receipt_timestamp timestamp static,
       receipt_total decimal static,
       receipt_id bigint,
       scan_id timeuuid,
       product_id text,
       product_name text,
       quantity int,
       unit_price decimal,
       total decimal,
       PRIMARY KEY (receipt_id, scan_id)
);

CREATE TABLE IF NOT EXISTS retail.receipts_by_store_date (
     store_id int,
     receipt_date timestamp,
     receipt_timestamp timestamp,
     register_id int,
     receipt_id bigint,
     receipt_total decimal,
     PRIMARY KEY ((store_id, receipt_date), register_id, receipt_timestamp, receipt_id)
);

CREATE TABLE IF NOT EXISTS retail.receipts_by_credit_card (
       credit_card_number bigint,
       receipt_timestamp timestamp,
       receipt_id bigint,
       credit_card_type text static,
       receipt_total decimal,
       store_id int,
       PRIMARY KEY ((credit_card_number), receipt_timestamp, receipt_id))
       WITH CLUSTERING ORDER BY (receipt_timestamp DESC, receipt_id DESC)
;

CREATE TABLE IF NOT EXISTS retail.inventory_per_store (
     store_date timestamp,
     store_id   int,
     product_id text,
     inventory  counter,
     PRIMARY KEY ((store_id, store_date), product_id)
);

CREATE TABLE IF NOT EXISTS retail.stores (
   store_id int,
   address text,
   address_2 text,
   address_3 text,
   city text,
   state text,
   zip bigint,
   size_in_sf int,
   PRIMARY KEY (store_id)
);


-- static data

-- Sample Data:
-- MISSING
CREATE TABLE IF NOT EXISTS retail.zipcodes (
    zipcode text,
    city text,
    state text,
    lat float,
    long float,
    population bigint,
    wages bigint,
    PRIMARY KEY (zipcode, city)
);

CREATE TABLE IF NOT EXISTS retail.sales_by_date (
    dummy text,
    sales_date timestamp,
    receipts_total decimal,
    PRIMARY KEY (dummy,sales_date)
);

CREATE TABLE IF NOT EXISTS retail.sales_by_state (
    dummy text,
    state text,
    region text,
    receipts_total decimal,
    PRIMARY KEY (dummy, receipts_total, state))
    WITH CLUSTERING ORDER BY (receipts_total DESC, state ASC)
;

CREATE TABLE IF NOT EXISTS retail.real_time_analytics (
  series text,
  timewindow timestamp,
  quantities map<text, int>,
  PRIMARY KEY ((series), timewindow)
) WITH CLUSTERING ORDER BY (timewindow DESC) AND
gc_grace_seconds=3600 AND
default_time_to_live=3600
;

