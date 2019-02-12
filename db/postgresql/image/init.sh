#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    CREATE USER aggregate WITH PASSWORD 'aggregate';
    CREATE DATABASE aggregate WITH OWNER aggregate;
    GRANT ALL PRIVILEGES ON DATABASE aggregate TO aggregate;
    \connect aggregate;
    CREATE SCHEMA aggregate;
    ALTER SCHEMA aggregate OWNER TO aggregate;
    GRANT ALL PRIVILEGES ON SCHEMA aggregate TO aggregate;
EOSQL
