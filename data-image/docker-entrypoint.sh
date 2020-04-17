#!/bin/bash

# wait for Minio to come up
echo "Wait for Minio to be up"
sleep 5

# copy all files in /opt/data/minio to Minio
echo "Copy files to Minio"
for filename in /opt/data/minio/*; do
  /opt/mc cp $filename minio/sql-demo
done

# start producing into kafka
echo "Start producing to Kafka: " $KAFKA_HOST
java -classpath /opt/data/data-producer.jar com.ververica.sqldemo.producer.TpchRecordProducer --input file /opt/data/kafka --output kafka $KAFKA_HOST --speedup $SPEEDUP
