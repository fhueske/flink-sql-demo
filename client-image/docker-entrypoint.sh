#!/bin/bash

INIT_DIR="/opt/sql-client/init"

# Wait for metastore if we need to
METASTORE="${METASTORE_HOST}:${METASTORE_PORT}"
if [ "$METASTORE" != ":" ]; then

  echo "Waiting for metastore to come up at " $METASTORE
  CONNECTED=false
  while ! $CONNECTED; do

    curl $METASTORE > /dev/null 2> /dev/null
    curlResult=$?

    # CURL code 52 means metastore is up
    if [ $curlResult == '52' ]; then
      CONNECTED=true
    fi

    echo "."
    sleep 1
  done;
  echo "Metastore is there!"
fi

echo "Start initialization"
if [ -d "$INIT_DIR" ]; then
	echo "Init folder found at " $INIT_DIR
	for initfile in "$INIT_DIR"/*.sql; do
		echo "Running init file: " $initfile
		cat $initfile | /opt/sql-client/sql-client.sh
	done
else
	echo "No init folder found at " $INIT_DIR
fi
echo "Initialization finished"

tail -f /dev/null