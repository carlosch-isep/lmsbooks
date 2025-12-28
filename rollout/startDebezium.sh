#!/bin/bash

# This script starts a Debezium connector for a PostgreSQL database.
curl -X POST http://localhost:8083/connectors \
     -H "Content-Type: application/json" \
     -d '{
           "name": "books-connector",
           "config": {
             "connector.class": "io.debezium.connector.postgresql.PostgresConnector",
             "database.hostname": "postgres_in_lms_network",
             "database.port": "5432",
             "database.user": "postgres",
             "database.password": "password",
             "database.dbname": "books_1",
             "database.server.name": "books",
             "plugin.name": "pgoutput",
             "slot.name": "debezium",
             "publication.name": "dbz_publication"
           }
         }'