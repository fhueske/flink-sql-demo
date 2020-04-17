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

FROM openjdk:8-jre

# Copy binary distributions from Apache servers/mirrors
RUN cd /opt && \
    wget http://apache.lauf-forum.at/hadoop/common/hadoop-2.9.2/hadoop-2.9.2.tar.gz && \
    wget http://archive.apache.org/dist/db/derby/db-derby-10.10.2.0/db-derby-10.10.2.0-bin.tar.gz && \
    wget http://apache.lauf-forum.at/hive/hive-2.3.6/apache-hive-2.3.6-bin.tar.gz

# Install Hadoop
RUN cd /opt && \
    tar xfz hadoop-2.9.2.tar.gz && \
    rm hadoop-2.9.2.tar.gz && \
# Install Derby
    tar xfz db-derby-10.10.2.0-bin.tar.gz && \
    rm db-derby-10.10.2.0-bin.tar.gz && \
    mkdir db-derby-10.10.2.0-bin/data && \
# Install Hive
    tar xfz apache-hive-2.3.6-bin.tar.gz && \
    rm apache-hive-2.3.6-bin.tar.gz && \
    cp /opt/db-derby-10.10.2.0-bin/lib/derbyclient.jar /opt/apache-hive-2.3.6-bin/lib && \
    cp /opt/db-derby-10.10.2.0-bin/lib/derbytools.jar /opt/apache-hive-2.3.6-bin/lib 

COPY hive-conf/hive-site.xml /opt/apache-hive-2.3.6-bin/conf

# Copy metastore_db backup file and restore it
COPY ./metastore_db_backup.tar /
RUN cd /; \
    tar xf /metastore_db_backup.tar; \
    rm /metastore_db_backup.tar;
    
ENV HADOOP_HOME /opt/hadoop-2.9.2
ENV DERBY_INSTALL /opt/db-derby-10.10.2.0-bin
ENV DERBY_HOME /opt/db-derby-10.10.2.0-bin

COPY docker-entrypoint.sh /
ENTRYPOINT ["/docker-entrypoint.sh"]