#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    CREATE USER odk WITH PASSWORD 'odk';
    CREATE DATABASE odk WITH OWNER odk;
    GRANT ALL PRIVILEGES ON DATABASE odk TO odk;
    \connect odk;
    CREATE SCHEMA aggregate;
    GRANT ALL PRIVILEGES ON SCHEMA aggregate TO odk;
EOSQL
