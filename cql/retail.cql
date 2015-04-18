CREATE KEYSPACE IF NOT EXISTS retail
WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };

-- static data

-- Sample Data:
-- brand: DSO Eyewear
CREATE TABLE IF NOT EXISTS retail.brands (
    brand text,

    PRIMARY KEY (
        brand
    )
);

-- static data

-- Sample Data:
-- product_id: B0009IB8RC
-- brand: null
-- title: Plantronics Headband with Tripod for CS50
-- price: 9.73
CREATE TABLE IF NOT EXISTS retail.products (
    product_id text,
    brand text,
    title text,
    price decimal,

    PRIMARY KEY (
        product_id
    )
);

-- static data

-- Sample Data:
-- MISSING
CREATE TABLE IF NOT EXISTS retail.zipcodes (
    zipcode text,
    city text,

    state text,
    lat decimal,
    long decimal,
    population bigint,
    wages bigint,

    PRIMARY KEY (
        zipcode,
        city
    )
);


-- pre-generated data

-- Sample Data:
-- store_id: 1001
-- tax_rate: 0.0825
-- express_registers: 4
-- full_registers: 20
-- street: 1500 Crossing Place
-- city: Austin
-- zipcode: 78741
-- phones: {
--     phone: 1234567890
--     pharmacy: 1234567890
--     fax: 1234567890
-- }
-- hours_open: 0600
-- hours_close: 2400
-- hours_exceptions: Open until 1am Friday and Saturday.
CREATE TABLE IF NOT EXISTS retail.stores (
    store_id int,

    tax_rate decimal,

    express_registers int,
    full_registers int,

    street text,
    city text,
    state text,
    zipcode text,
    phones map<text, text>,

    hours_open int,
    hours_close int,
    hours_exceptions text,

    PRIMARY KEY (
        store_id
    )
);


-- pre-generated data

-- Sample Data:
-- employee_id: 10001
-- store_id: <retail.stores.store_id>
-- first_name: Joaquin
-- last_name: Casares
-- last_initial: C
CREATE TABLE IF NOT EXISTS retail.employees (
    employee_id int,

    store_id int,
    first_name text,
    last_name text,
    last_initial text,

    PRIMARY KEY (
        employee_id
    )
);


-- dynamic data

-- Sample Data:
-- store_id: <retail.stores.store_id>
-- register_id: <range(express_registers + full_registers)>
-- receipt_id: <retail.receipts.receipt_id>
-- scan_time: ${TIMESTAMP}
-- quantity: 2
-- brand: <retail.products.brand>
-- product_id: <retail.products.product_id>
-- product: <retail.products.product>
-- price: <cluster(retail.products.price)>
-- msrp: <retail.products.price>
-- savings: <msrp - price>
CREATE TABLE IF NOT EXISTS retail.registers (
    store_id int,
    register_id int,
    receipt_id timeuuid,
    scan_time timestamp,

    scan_duration int,
    quantity decimal,

    brand text,
    product_id text,
    product text,

    price decimal,
    msrp decimal,
    savings decimal,

    PRIMARY KEY (
        store_id,
        register_id,
        receipt_id,
        scan_time
    )
);

-- dynamic data

-- Sample Data:
-- receipt_id: ${TIMEUUID}
-- store_id: <retail.stores.store_id>
-- register_id: <retail.registers.register_id>
-- cashier_id: <retail.employees.employee_id>
-- cashier_first_name: <retail.employees.first_name>
-- cashier_last_name: <retail.employees.last_name>
-- cashier_last_initial: <retail.employees.last_initial>
-- close_drawer: ${TIMESTAMP}
-- subtotal: <sum(retail.registers.price)>
-- tax_rate: <retail.stores.tax_rate>
-- total: <subtotal + subtotal * tax_rate>
-- savings: <sum(retail.registers.savings)>
-- payment: {
--     type: AMEX
--     account: **** ***** 1112
--     approval_no: 548411
--     reference_no: 670271
-- }
CREATE TABLE IF NOT EXISTS retail.receipts (
    receipt_id timeuuid,

    store_id int,
    register_id int,

    cashier_id int,
    cashier_first_name text,
    cashier_last_name text,
    cashier_last_initial text,

    close_drawer timestamp,

    subtotal decimal,
    tax_rate decimal,
    total decimal,
    savings decimal,

    payment map<text,text>,

    PRIMARY KEY (
        receipt_id
    )
);


CREATE KEYSPACE IF NOT EXISTS metagener
WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 3 };

CREATE TABLE IF NOT EXISTS metagener.futures (
    now int,
    store_id int,
    register_id int,
    receipt_id timeuuid,

    PRIMARY KEY (
        now,
        store_id,
        register_id,
        receipt_id
    )
);

CREATE TABLE IF NOT EXISTS metagener.active (
    store_id int,
    register_id int,

    PRIMARY KEY (
        store_id,
        register_id
    )
);
