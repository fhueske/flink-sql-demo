#!/bin/bash

# start Derby
/opt/db-derby-10.10.2.0-bin/bin/startNetworkServer -h 0.0.0.0 &

# create Metastore schema
/opt/apache-hive-2.3.6-bin/bin/schematool -initSchema -dbType derby

# start Metastore
/opt/apache-hive-2.3.6-bin/bin/hive --service metastore -p 9083
