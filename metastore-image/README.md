# Hive Metastore Docker Image

This repository contains a Dockerfile to build a Docker image for Hive Metastore.

## Building the image

```
docker build -t fhueske/metastore:2.3.4 .
```

## Starting the image

```
docker run -d -p 9083:9083 fhueske/metastore:2.3.4
```

## Connecting to Metastore

Point the Hive client to Hive configuration at `./hive-client-conf`.