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

FROM openjdk:8-jre

ADD VERSION .

# Copy data producer
COPY --from=builder /opt/data-producer/target/sql-demo-data-producer-*.jar /opt/data/data-producer.jar

# Create data folders
RUN mkdir -p /opt/data; \
    mkdir -p /opt/data/minio; \
    mkdir -p /opt/data/kafka;

# get mc and configure (minio client)
RUN cd /opt; \
    wget https://dl.min.io/client/mc/release/linux-amd64/mc; \
    chmod +x mc

COPY docker-entrypoint.sh /
ENTRYPOINT ["/docker-entrypoint.sh"]
