###############################################################################
#  Licensed to the Apache Software Foundation (ASF) under one
#  or more contributor license agreements.  See the NOTICE file
#  distributed with this work for additional information
#  regarding copyright ownership.  The ASF licenses this file
#  to you under the Apache License, Version 2.0 (the
#  "License"); you may not use this file except in compliance
#  with the License.  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
# limitations under the License.
###############################################################################

###############################################################################
# Build Click Count Job
###############################################################################

FROM maven:3.6-jdk-8-slim AS builder

# Get UDF code and compile it
COPY ./java/sql-demo-udfs /opt/sql-udfs
RUN cd /opt/sql-udfs; \
    mvn clean install

###############################################################################
# Build SQL Playground Image
###############################################################################

FROM flink:1.10.0-scala_2.11

ADD VERSION .

# Copy sql-client configuration
COPY sql-client/ /opt/sql-client

# Copy playground UDFs
COPY --from=builder /opt/sql-udfs/target/sql-demo-udfs-*.jar /opt/sql-client/lib/

# Download connector libraries
RUN wget -P /opt/sql-client/lib/ https://repo.maven.apache.org/maven2/org/apache/flink/flink-json/${FLINK_VERSION}/flink-json-${FLINK_VERSION}.jar; \
    wget -P /opt/sql-client/lib/ https://repo.maven.apache.org/maven2/org/apache/flink/flink-sql-connector-kafka_2.11/${FLINK_VERSION}/flink-sql-connector-kafka_2.11-${FLINK_VERSION}.jar; \
    wget -P /opt/sql-client/lib/ https://repo.maven.apache.org/maven2/org/apache/flink/flink-jdbc_2.11/1.10.0/flink-jdbc_2.11-1.10.0.jar; \
    wget -P /opt/sql-client/lib/ https://repo.maven.apache.org/maven2/org/apache/flink/flink-csv/1.10.0/flink-csv-1.10.0.jar; \
    wget -P /opt/sql-client/lib/ https://repo.maven.apache.org/maven2/mysql/mysql-connector-java/8.0.19/mysql-connector-java-8.0.19.jar;

# Download Hive dependencies
RUN wget -P /opt/sql-client/lib https://repo.maven.apache.org/maven2/org/apache/flink/flink-connector-hive_2.11/1.10.0/flink-connector-hive_2.11-1.10.0.jar; \
    wget -P /opt/sql-client/lib https://repo.maven.apache.org/maven2/org/apache/hive/hive-exec/2.3.6/hive-exec-2.3.6.jar;

# get Hadoop for Hive
RUN wget -P /opt/flink/lib https://repo.maven.apache.org/maven2/org/apache/flink/flink-shaded-hadoop-2-uber/2.7.5-8.0/flink-shaded-hadoop-2-uber-2.7.5-8.0.jar;

# Copy configuration
COPY conf/* /opt/flink/conf/
COPY hive-conf /opt/hive-conf

WORKDIR /opt/sql-client
ENV SQL_CLIENT_HOME /opt/sql-client

COPY docker-entrypoint.sh /
ENTRYPOINT ["/docker-entrypoint.sh"]