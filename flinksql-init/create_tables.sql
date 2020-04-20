USE CATALOG hcat;

CREATE TABLE dev_region (
  r_regionkey     INTEGER,
  r_name          STRING,
  r_comment       STRING
) WITH (
  'connector.type' = 'filesystem',
  'connector.path' = 's3://sql-demo/region.tbl',
  'format.type' = 'csv',
  'format.field-delimiter' = '|'
);

CREATE TABLE prod_region (
  r_regionkey     INTEGER,
  r_name          STRING,
  r_comment       STRING
) WITH (
  'connector.type' = 'jdbc',
  'connector.url' = 'jdbc:mysql://mysql:3306/sql-demo',
  'connector.table' = 'PROD_REGION',
  'connector.driver' = 'com.mysql.jdbc.Driver',
  'connector.username' = 'sql-demo',
  'connector.password' = 'demo-sql',
  'connector.lookup.cache.max-rows' = '1',
  'connector.lookup.cache.ttl' = '0s'
);

CREATE TABLE dev_nation (
  n_nationkey     INTEGER,
  n_name          STRING,
  n_regionkey     INTEGER,
  n_comment       STRING
) WITH (
  'connector.type' = 'filesystem',
  'connector.path' = 's3://sql-demo/nation.tbl',
  'format.type' = 'csv',
  'format.field-delimiter' = '|'
);

CREATE TABLE prod_nation (
  n_nationkey     INTEGER,
  n_name          STRING,
  n_regionkey     INTEGER,
  n_comment       STRING
) WITH (
  'connector.type' = 'jdbc',
  'connector.url' = 'jdbc:mysql://mysql:3306/sql-demo',
  'connector.table' = 'PROD_NATION',
  'connector.driver' = 'com.mysql.jdbc.Driver',
  'connector.username' = 'sql-demo',
  'connector.password' = 'demo-sql',
  'connector.lookup.cache.max-rows' = '1',
  'connector.lookup.cache.ttl' = '0s'
);

CREATE TABLE dev_customer (
  c_custkey       INTEGER,
  c_name          STRING,
  c_address       STRING,
  c_nationkey     INTEGER,
  c_phone         STRING,
  c_acctbal       DOUBLE,
  c_mktsegment    STRING,
  c_comment       STRING
) WITH (
  'connector.type' = 'filesystem',
  'connector.path' = 's3://sql-demo/customer.tbl',
  'format.type' = 'csv',
  'format.field-delimiter' = '|'
);

CREATE TABLE prod_customer (
  c_custkey       INTEGER,
  c_name          STRING,
  c_address       STRING,
  c_nationkey     INTEGER,
  c_phone         STRING,
  c_acctbal       DOUBLE,
  c_mktsegment    STRING,
  c_comment       STRING
) WITH (
  'connector.type' = 'jdbc',
  'connector.url' = 'jdbc:mysql://mysql:3306/sql-demo',
  'connector.table' = 'PROD_CUSTOMER',
  'connector.driver' = 'com.mysql.jdbc.Driver',
  'connector.username' = 'sql-demo',
  'connector.password' = 'demo-sql',
  'connector.lookup.cache.max-rows' = '1',
  'connector.lookup.cache.ttl' = '0s'
);

CREATE TABLE dev_rates (
  rs_timestamp    TIMESTAMP(3),
  rs_symbol       STRING,
  rs_rate         DOUBLE
) WITH (
  'connector.type' = 'filesystem',
  'connector.path' = 's3://sql-demo/rates.tbl',
  'format.type' = 'csv',
  'format.field-delimiter' = '|'
);

CREATE TABLE prod_rates (
  rs_timestamp    TIMESTAMP(3),
  rs_symbol       STRING,
  rs_rate         DOUBLE
) WITH (
  'connector.type' = 'jdbc',
  'connector.url' = 'jdbc:mysql://mysql:3306/sql-demo',
  'connector.table' = 'PROD_RATES',
  'connector.driver' = 'com.mysql.jdbc.Driver',
  'connector.username' = 'sql-demo',
  'connector.password' = 'demo-sql',
  'connector.lookup.cache.max-rows' = '1',
  'connector.lookup.cache.ttl' = '0s'
);

CREATE TABLE dev_orders (
  o_orderkey      INTEGER,
  o_custkey       INTEGER,
  o_orderstatus   STRING,
  o_totalprice    DOUBLE,
  o_currency      STRING,
  o_ordertime     TIMESTAMP(3),
  o_orderpriority STRING,
  o_clerk         STRING, 
  o_shippriority  INTEGER,
  o_comment       STRING,
  WATERMARK FOR o_ordertime AS o_ordertime - INTERVAL '5' MINUTE
) WITH (
  'connector.type' = 'filesystem',
  'connector.path' = 's3://sql-demo/orders.tbl',
  'format.type' = 'csv',
  'format.field-delimiter' = '|'
);

CREATE TABLE dev_lineitem (
  l_orderkey       INTEGER,
  l_partkey        INTEGER,
  l_suppkey        INTEGER,
  l_linenumber     INTEGER,
  l_quantity       DOUBLE,
  l_extendedprice  DOUBLE,
  l_discount       DOUBLE,
  l_tax            DOUBLE,
  l_currency       STRING,
  l_returnflag     STRING,
  l_linestatus     STRING,
  l_ordertime      TIMESTAMP(3),
  l_shipinstruct   STRING,
  l_shipmode       STRING,
  l_comment        STRING
) WITH (
  'connector.type' = 'filesystem',
  'connector.path' = 's3://sql-demo/lineitem.tbl',
  'format.type' = 'csv',
  'format.field-delimiter' = '|'
);

