# Scenario and Data

* What do we show in this demo
  * Flink SQL processing data from different storage systems
  * Flink SQL using Hive Metastore as an external, persistent catalog
  * Batch/Stream unification of queries in action
  * Different ways to join dynamic data
  * Creating Tables with DDL
  * Maintaining materialize views with continuous SQL queries in Kafka and MySQL

* Scenario is an online store receiving orders.
* The order system consists of six tables (derived from the well-known TPC-H benchmark)
  * `PROD_ORDERS` one row for each order
  * `PROD_LINEITEM`: individual items of an order
  * `PROD_CUSTOMER`: customer data
  * `PROD_NATION`: denormalized nation data
  * `PROD_REGION`: denormalized region data
  * `PROD_RATES`: exchange rates for currencies
  * `PROD_RATES_HISTORY`: history of all currency exchange rates

Depending on their update characteristic (frequency, insert-only) tables are stored in different systems:
* Kafka: `PROD_ORDERS`, `PROD_LINEITEM`, `PROD_RATES_HISTORY`
* MySQL: `PROD_CUSTOMER`, `PROD_NATION`, `PROD_REGION`, `PROD_RATES`

# Get the Data

Please download the demo data from Google Drive and extract the zip archive into the `./data` folder (as `./data/*.tbl`).

https://drive.google.com/file/d/15LWUBGZenWaW3R_WNxqvcTQOuA_Kgtjv

The folder will be mounted by the Docker containers.

# Build the Docker Images

```
docker-compose build
```

# Start the Docker Environment

```
docker-compose up -d
```

# Show the Demo Environment

The demo environment consists of the following components.

## Kafka + Data Provider (+ Zookeeper)
    
* Show Kafka data streams

```
docker-compose exec kafka kafka-console-consumer.sh --bootstrap-server kafka:9092 --from-beginning --topic orders
docker-compose exec kafka kafka-console-consumer.sh --bootstrap-server kafka:9092 --from-beginning --topic lineitem
docker-compose exec kafka kafka-console-consumer.sh --bootstrap-server kafka:9092 --from-beginning --topic rates
```

## MySQL

* Show data in MySQL

```
docker-compose exec mysql mysql -Dsql-demo -usql-demo -pdemo-sql

SHOW TABLES;
DESCRIBE PROD_CUSTOMER;
SELECT * FROM PROD_CUSTOMER LIMIT 10;
quit;
```

## Grafana

* Dashboard tool: [http://localhost:3000](http://localhost:3000)

## Minio (S3-compatible Storage)

* No files here yet
* Show in Web UI: [http://localhost:9000](http://localhost:9000) (user: `sql-demo`, password: `demo-sql`)

## Flink JobManager and TaskManager

* Queries are executed on a FLink cluster
* Show Web UI: [http://localhost:8081](http://localhost:8081)

## SQL client + Hive Metastore

* SQL client provides SQL environment and submits queries to Flink for execution
* Hive Metastore presistently stores catalog data

# The Demo

## Start the SQL Client

```
docker-compose exec sql-client ./sql-client.sh
```

## Show Tables

All dynamic (Kafka-backed) tables are in the default catalog:

```
SHOW TABLES;
DESCRIBE prod_orders;
SELECT * FROM prod_orders;
```

All static (MySQL-backed) tables are in the Hive catalog:

```
USE CATALOG hive;
SHOW TABLES;
DESCRIBE prod_nation;
SELECT * FROM prod_nation;
```

## Running Queries on Static and Dynamic Tables

* Flink SQL unifies batch and stream processing.
* Many queries can be executed on static and dynamic table.

### Take a Snapshot of a Dynamic Table

* Create a table backed by a file in S3.

```
USE CATALOG hive;
```

```
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
```

* Write a sample of a dynamic streaming table to the static table.

```
INSERT INTO dev_orders SELECT * FROM default_catalog.default_database.prod_orders;
```

* Show running job in Flink Web UI [http://localhost:8081](http://localhost:8081)
* Manually cancel the job
* Show file in Minio: [http://localhost:9000](http://localhost:9000)
* Show data in SQL client

```
SELECT * FROM dev_orders;
SELECT COUNT(*) AS rowCnt FROM dev_orders;
```

### Run a Query on the Snapshotted Data

* Using the batch engine for better efficiency

```
SET execution.type=batch;
```

* Compute revenue per currency and minute

```
SELECT
  CEIL(o_ordertime TO MINUTE) AS `minute`,
  o_currency AS `currency`,
  SUM(o_totalprice) AS `revenue`,
  COUNT(*) AS `orderCnt`
FROM dev_orders
GROUP BY
  o_currency,
  CEIL(o_ordertime TO MINUTE);
```

* Run the same query as a continuous streaming query

```
SET execution.type=streaming;
```

* Show why we should streamify the query

```
SET execution.result-mode=changelog;
```

* We can streamify the query a bit with a TUMBLE window

```
SELECT
  TUMBLE_END(o_ordertime, INTERVAL '1' MINUTE) AS `minute`,
  o_currency AS `currency`,
  SUM(o_totalprice) AS `revenue`,
  COUNT(*) AS `orderCnt`
FROM dev_orders
GROUP BY
  o_currency,
  TUMBLE(o_ordertime, INTERVAL '1' MINUTE);
```

* We can execute this query also with the batch engine
* Reset to previous settings

```
SET execution.result-mode=table;
SET execution.type=batch;
```

### Move query to streaming data

* We can run the same query on the table backed by a Kafka topic

```
SET execution.type=streaming;
```

* We only change the table in the FROM clause

```
SELECT
  TUMBLE_END(o_ordertime, INTERVAL '1' MINUTE) AS `minute`,
  o_currency AS `currency`,
  SUM(o_totalprice) AS `revenue`,
  COUNT(*) AS `orderCnt`
FROM default_catalog.default_database.prod_orders
GROUP BY
  o_currency,
  TUMBLE(o_ordertime, INTERVAL '1' MINUTE);
```

## Five Ways to Join Tables

* Dynamic tables can be joined just like static tables. 
  * However, joins can be inefficient, if you ignore the dynamic property of tables.
  * There are common patterns to join that can be efficiently executed with low resource consumption.

### Regular Join on Static Tables

```
SET execution.type=batch;
```

```
USE CATALOG hive;
```

* show customers and their orders by region and priority for a specific day
* query joins the snapshot of the orders table

```
SELECT
  r_name AS `region`,
  o_orderpriority AS `priority`,
  COUNT(DISTINCT c_custkey) AS `number_of_customers`,
  COUNT(o_orderkey) AS `number_of_orders`
FROM dev_orders
JOIN prod_customer ON o_custkey = c_custkey
JOIN prod_nation ON c_nationkey = n_nationkey
JOIN prod_region ON n_regionkey = r_regionkey
WHERE
  FLOOR(o_ordertime TO DAY) = TIMESTAMP '2020-04-01 0:00:00.000'
  AND NOT o_orderpriority = '4-NOT SPECIFIED'
GROUP BY r_name, o_orderpriority
ORDER BY r_name, o_orderpriority;
```

### Regular Join on Dynamic Tables

* We can run the same query on the `prod_orders` table 
* Change `dev_orders` to `prod_orders`
* Remove `ORDER BY` clause

```
SET execution.type=streaming;
```

```
USE CATALOG hive;
```

```
SELECT
  r_name AS `region`,
  o_orderpriority AS `priority`,
  COUNT(DISTINCT c_custkey) AS `number_of_customers`,
  COUNT(o_orderkey) AS `number_of_orders`
FROM default_catalog.default_database.prod_orders
JOIN prod_customer ON o_custkey = c_custkey
JOIN prod_nation ON c_nationkey = n_nationkey
JOIN prod_region ON n_regionkey = r_regionkey
WHERE
  FLOOR(o_ordertime TO DAY) = TIMESTAMP '2020-04-01 0:00:00.000'
  AND NOT o_orderpriority = '4-NOT SPECIFIED'
GROUP BY r_name, o_orderpriority;
```

* Notable properties of this join
  * The batch tables are read just once. Updates are not considered!
  * All input tables are completely materialized in state.

### Interval Join


* A common requirement is to join events of two (or more) dynamic tables that are related with each other in a temporal context, for example events that happened around the same time.
* Flink SQL features special optimizations for such joins.
* First switch to the default catalog (which contains all dynamic tables)

```
USE CATALOG default_catalog;
```

* Join `prod_lineitem` and `prod_orders` to compute open lineitems with urgent order priority.

```
SELECT
  o_ordertime AS `ordertime`,
  o_orderkey AS `order`,
  l_linenumber AS `linenumber`,
  l_partkey AS `part`,
  l_suppkey AS `supplier`,
  l_quantity AS `quantity`
FROM prod_lineitem
JOIN prod_orders ON o_orderkey = l_orderkey
WHERE
  l_ordertime BETWEEN o_ordertime - INTERVAL '5' MINUTE AND o_ordertime AND
  l_linestatus = 'O' AND
  o_orderpriority = '1-URGENT';
```

* Notable properties of the join
  * Event-time semantics (processing time also supported)
  * Small state requirements. Only the 5 minute tails of the streams need to be held in state.

### Enrichment Join with Lookup Table in MySQL

* Enriching an insert-only, dynamic table with data from a static or slowly changing table

* Example query: enrich `prod_lineitem` table with current exchange rate from `prod_rates` table to compute EUR-normalize amounts.

```
USE CATALOG default_catalog;
```

```
SELECT
  l_proctime AS `querytime`,
  l_orderkey AS `order`,
  l_linenumber AS `linenumber`,
  l_currency AS `currency`,
  rs_rate AS `cur_rate`, 
  (l_extendedprice * (1 - l_discount) * (1 + l_tax)) / rs_rate AS `open_in_euro`
FROM prod_lineitem
JOIN hive.`default`.prod_rates FOR SYSTEM_TIME AS OF l_proctime ON rs_symbol = l_currency
WHERE
  l_linestatus = 'O';
```

* Check `PROD_RATES` table in MySQL and update a rate

```
docker-compose exec mysql mysql -Dsql-demo -usql-demo -pdemo-sql
SELECT * FROM PROD_RATES;
UPDATE PROD_RATES SET RS_TIMESTAMP = '2020-04-01 01:00:00.000', RS_RATE = 1.234 WHERE RS_SYMBOL='USD';
```

* Notable properties of the join
  * Processing time semantics: rates are looked up from MySQL table as of the processing time of a row
  * MySQL table can be updated while the job is running

### Enrichment Join against Temporal Table

* Same use case as before. 
* Instead of looking up rates from MySQL, we ingest updates from another Kafka table.
* Kafka table is accessed via a TemporalTableFunction `prod_rates_temporal` which looks up the most-recent exchange rate for a currency.

```
USE CATALOG default_catalog;
```

```
SELECT
  l_ordertime AS `ordertime`,
  l_orderkey AS `order`,
  l_linenumber AS `linenumber`,
  l_currency AS `currency`,
  rs_rate AS `cur_rate`, 
  (l_extendedprice * (1 - l_discount) * (1 + l_tax)) / rs_rate AS `open_in_euro`
FROM
  prod_lineitem,
  LATERAL TABLE(prod_rates_temporal(l_ordertime))
WHERE rs_symbol = l_currency AND
  l_linestatus = 'O';
```

* Notable properties of the join
  * Event time semantics: rates are looked up from temporal table (backed by a Kafka topic) as of the order time of the item.
  * Rates change as records are produced into Kafka topic

## Matching Patterns on Dynamic Tables 

* Matching patterns on new data with low latency is a common use case for stream processing
* SQL:2016 introduced the `MATCH_RECOGNIZE` clause to match patterns on tables
* The combination of `MATCH_RECOGNIZE` and dynamic tables is very powerful
* Flink supports `MATCH_RECOGNIZE` since several releases

* Find customers that have changed their delivery behavior.
  * Search for a pattern where the last x lineitems had regular shippings
  * But now the customer whats to pay on delivery (collect on delivery = CoD)

```
USE CATALOG default_catalog;
```

```
CREATE VIEW lineitem_with_customer AS
SELECT * FROM (
  SELECT
    o_custkey AS `custkey`,
    l_orderkey AS `orderkey`,
    l_linenumber AS `linenumber`,
    l_ordertime AS `ordertime`,
    l_shipinstruct AS `shipinstruct`
  FROM prod_lineitem
  JOIN prod_orders ON o_orderkey = l_orderkey
  WHERE
    l_ordertime BETWEEN o_ordertime - INTERVAL '5' MINUTE AND o_ordertime
);
```

```
SELECT *
FROM lineitem_with_customer
MATCH_RECOGNIZE(
  PARTITION BY custkey
  ORDER BY ordertime
  MEASURES
    COUNT(OTHER.orderkey) AS `last regular shipings`,
    LAST(OTHER.orderkey) AS `last regular order`,
    LAST(OTHER.linenumber) AS `last regular linenumber`,
    COD.orderkey AS `cod order`,
    COD.linenumber AS `cod linenumber`
  PATTERN (OTHER{5,} COD)
  DEFINE
    OTHER AS NOT shipinstruct = 'COLLECT COD',
    COD AS shipinstruct = 'COLLECT COD'
);
```

## Maintaining Materialized Views

* Continuous queries on dynamic tables update their result with low latency.
* Results can be maintained in external systems similar to materialized views.

### Writing Results to Kafka

* Let's write some data back to Kafka
* We use the window aggregation query that we've discussed before

```
USE CATALOG default_catalog;
```

```
SELECT
  TUMBLE_END(o_ordertime, INTERVAL '1' MINUTE) AS `minute`,
  o_currency AS `currency`,
  SUM(o_totalprice) AS `revenue`,
  COUNT(*) AS `orderCnt`
FROM default_catalog.default_database.prod_orders
GROUP BY
  o_currency,
  TUMBLE(o_ordertime, INTERVAL '1' MINUTE);
```

* Create a Kafka-backed table `minute_stats` with JSON encoding

```
CREATE TABLE minute_stats (
  `minute` TIMESTAMP(3),
  `currency` STRING,
  `revenueSum` DOUBLE,
  `orderCnt` BIGINT,
  WATERMARK FOR `minute` AS `minute` - INTERVAL '10' SECOND
) WITH (
  'connector.type' = 'kafka',       
  'connector.version' = 'universal',
  'connector.topic' = 'minute_stats',
  'connector.properties.zookeeper.connect' = 'not-needed',
  'connector.properties.bootstrap.servers' = 'kafka:9092',
  'connector.startup-mode' = 'earliest-offset',
  'format.type' = 'json'
);
```

* Automatic topic creation is enabled in Kafka

* Write query result into table

```
INSERT INTO minute_stats
SELECT
  TUMBLE_END(o_ordertime, INTERVAL '1' MINUTE) AS `minute`,
  o_currency AS `currency`,
  SUM(o_totalprice) AS `revenue`,
  COUNT(*) AS `orderCnt`
FROM default_catalog.default_database.prod_orders
GROUP BY
  o_currency,
  TUMBLE(o_ordertime, INTERVAL '1' MINUTE);
```

* Check results in Kafka

```
docker-compose exec kafka kafka-console-consumer.sh --bootstrap-server kafka:9092 --from-beginning --topic minute_stats
```

### Writing Results to MySQL

* We would like to store and mainain the result of the following query in MySQL.

```
USE CATALOG hive;
```

```
SELECT
  r_name AS `region`,
  HOUR(o_ordertime) AS `hour_of_day`,
  COUNT(DISTINCT c_custkey) AS `number_of_customers`,
  COUNT(o_orderkey) AS `number_of_orders`
FROM (
  SELECT CAST(o_ordertime AS TIMESTAMP) o_ordertime, o_orderkey, o_custkey
  FROM default_catalog.default_database.prod_orders)
JOIN prod_customer ON o_custkey = c_custkey
JOIN prod_nation ON c_nationkey = n_nationkey
JOIN prod_region ON n_regionkey = r_regionkey
GROUP BY 
  r_name, 
  HOUR(o_ordertime);
```

* Create a table in Flink

```
CREATE TABLE region_stats (
  region               STRING,
  hour_of_day          BIGINT,
  number_of_customers  BIGINT,
  number_of_orders     BIGINT
) WITH (
  'connector.type' = 'jdbc',
  'connector.url' = 'jdbc:mysql://mysql:3306/sql-demo',
  'connector.table' = 'REGION_STATS',
  'connector.driver' = 'com.mysql.jdbc.Driver',
  'connector.username' = 'sql-demo',
  'connector.password' = 'demo-sql',
  'connector.write.flush.interval' = '2s',
  'connector.write.flush.max-rows' = '1000'
);
```

* The table is configured to emit updates every second (or every 1000 updates).

* Write query result to MySQL

```
INSERT INTO region_stats
SELECT
  r_name AS `region`,
  HOUR(o_ordertime) AS `hour_of_day`,
  COUNT(DISTINCT c_custkey) AS `number_of_customers`,
  COUNT(o_orderkey) AS `number_of_orders`
FROM (
  SELECT CAST(o_ordertime AS TIMESTAMP) o_ordertime, o_orderkey, o_custkey
  FROM default_catalog.default_database.prod_orders)
JOIN prod_customer ON o_custkey = c_custkey
JOIN prod_nation ON c_nationkey = n_nationkey
JOIN prod_region ON n_regionkey = r_regionkey
GROUP BY 
  r_name, 
  HOUR(o_ordertime);
```

#### Monitoring Results with Grafana

* Open Grafana: [http://localhost:3000](http://localhost:3000)
* Go to dashboard "Region Stats" and set refresh rate to 1s (top right corner) to see how the query updates the result table.

#### Monitoring Results with MySQL CLI

* Start MySQL CLI

```
docker-compose exec mysql mysql -Dsql-demo -usql-demo -pdemo-sql
```

* Repeatedly run the following query to see how the query updates the result table.

```
SELECT * FROM REGION_STATS ORDER BY hour_of_day, region;
```
