# Apache Flink® SQL Demo

**This repository provides a demo for Flink SQL.**

The demo shows how to:

* Setup Flink SQL with a Hive catalog.
* Use Flink SQL to prototype a query on a small CSV sample data set.
* Run the same query on a larger ORC data set.
* Run the same query as a continuous query on a Kafka topic.
* Run differnet streaming SQL queries including pattern matching with `MATCH_RECOGNIZE`
* Maintain a materialized view in MySQL

## Demo Setup

The demo environment is based on Docker Compose and starts the following containers:

* Flink SQL Client
* Flink JobManager
* Flink TaskManager
* Kafka
* Zookeeper (for Kafka)
* MySQL
* Minio (S3-compatible storage)
* A data provider to write data to Kafka and Minio

## Starting the demo

Before you run the demo, you need to build some custom images. This might take a few minutes.

```
docker-compose build
```

After all images are built, start the environment.

```
docker-compose up -d
```

Once all containers are up, you can start the SQL CLI Client

```
docker-compose exec sql-client ./sql-client.sh
```

## Demo Preparation Instructions for Timo (to be replaced by more detailed demo instructions)

* The data provider image is not working yet and does not push any data
* Find DDL statements to create tables and instructions to ingest data to/extract data from source and sink systems below:

### Metastore Catalog

* A Metastore catalog is configured and can be used.

```
USE CATALOG hcat;
```

Switch back to the default catalog:

```
use catalog default_catalog;
```

* Start Hive CLI client

```
docker-compose exec metastore /opt/apache-hive-2.3.6-bin/bin/hive
```

### Kafka

* Create Topics: add entries to Kafka section in `docker-compose.yaml`
* Create a Kafka-CSV table (here standard Orders with proctime column)

```
CREATE TABLE Orders (
  O_ORDERKEY INTEGER,
  O_CUSTKEY INTEGER,
  O_ORDERSTATUS CHAR(1),
  O_TOTALPRICE DECIMAL(15,2),
  O_ORDERDATE DATE,
  O_ORDERPRIORITY CHAR(15),  
  O_CLERK CHAR(15),
  O_SHIPPRIORITY INTEGER,
  O_COMMENT VARCHAR(79),
  PROC_TIME AS PROCTIME()
) WITH (
  'connector.type' = 'kafka',       
  'connector.version' = 'universal',
  'connector.topic' = 'Orders',
  'connector.properties.zookeeper.connect' = 'not-needed',
  'connector.properties.bootstrap.servers' = 'kafka:9092',
  'connector.startup-mode' = 'earliest-offset',
  'format.type' = 'csv',
  'format.field-delimiter' = '|'
);
```

* Write data to Kafka topic

```
docker-compose exec kafka kafka-console-producer.sh --broker-list kafka:9092 --topic Orders
```

### MySQL

* Start MySQL CLI client (to create tables, insert data, ...)

```
docker-compose exec mysql mysql -Dsql-demo -usql-demo -pdemo-sql
```

* Create a MySQL table that also works as LookupTableSource

```
CREATE TABLE ExchangeRates (
  currency STRING,
  rate DOUBLE
) WITH (
  'connector.type' = 'jdbc',
  'connector.url' = 'jdbc:mysql://mysql:3306/sql-demo',
  'connector.table' = 'ExchangeRates',
  'connector.driver' = 'com.mysql.jdbc.Driver',
  'connector.username' = 'sql-demo',
  'connector.password' = 'demo-sql',
  'connector.lookup.cache.max-rows' = '1',
  'connector.lookup.cache.ttl' = '0s'
);
```

* Add data to MySQL table either using the MySQL CLI client (see above). Writing from Flink CLI client with `INSERT INTO` works as well! :-)

### Minio

* The Minio WebUI can be accessed at [http://http://localhost:9000/minio/sql-demo|http://http://localhost:9000/minio/sql-demo]

* Create a Minio table with CSV format

```
CREATE TABLE orders (
  O_ORDERKEY INTEGER,
  O_CUSTKEY INTEGER,
  O_ORDERSTATUS CHAR(1),
  O_TOTALPRICE DECIMAL(15,2),
  O_ORDERDATE DATE,
  O_ORDERPRIORITY CHAR(15),  
  O_CLERK CHAR(15),
  O_SHIPPRIORITY INTEGER,
  O_COMMENT VARCHAR(79)
)
WITH (
  'connector.type' = 'filesystem',
  'connector.path' = 's3://sql-demo/orders.tbl',
  'format.type' = 'csv',
  'format.field-delimiter' = '|'
);
```

* Add data to Minio via its WebUI (see above).


----

*Apache Flink, Flink®, Apache®, the squirrel logo, Apache Kafka, Apache Hive, and the Apache feather logo are either registered trademarks or trademarks of The Apache Software Foundation.*
