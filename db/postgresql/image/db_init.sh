#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    CREATE USER "odk_unit" WITH UNENCRYPTED PASSWORD 'test';
    CREATE DATABASE odk_db WITH OWNER odk_unit;
    GRANT ALL PRIVILEGES ON DATABASE odk_db TO odk_unit;
    \connect odk_db;
    CREATE SCHEMA odk_db;
    GRANT ALL PRIVILEGES ON SCHEMA odk_db TO odk_unit;
EOSQL