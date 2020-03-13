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

# Get data producer code and compile it
COPY ./java/sql-demo-data-producer /opt/data-producer
RUN cd /opt/data-producer; \
    mvn clean install

###############################################################################
# Build SQL Playground Image
###############################################################################

FROM flink:1.10.0-scala_2.11

ADD VERSION .

# Copy data producer
COPY --from=builder /opt/data-producer/target/sql-demo-data-producer-*.jar /opt/data/data-producer.jar

# Create data folders
RUN mkdir -p /opt/data; \
    mkdir -p /opt/data/stream;
# Download data files
#    wget -O /opt/data/driverChanges.txt.gz 'https://drive.google.com/uc?export=download&id=1pf4tfv-YpoVQ9_O0948M8oXeCfVH-0MH'; \
#    wget -O /opt/data/fares.txt.gz 'https://drive.google.com/uc?export=download&id=1SriiwcIdMvY7uJsWSY4Hhh32iO3F4ND2'; \
#    wget -O /opt/data/rides.txt.gz 'https://drive.google.com/uc?export=download&id=1gY8W07OFvB7_4lHlAyingM4WQzs0_8lT';

WORKDIR /opt/sql-client
ENV SQL_CLIENT_HOME /opt/sql-client
