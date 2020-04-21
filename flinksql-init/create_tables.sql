USE CATALOG hive;

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

QUIT;
