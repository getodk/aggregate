create database "odk_unit";
SELECT datname FROM pg_database WHERE datistemplate = false;
create user "odk_unit" with unencrypted password 'odk_unit';
grant all privileges on database "odk_unit" to "odk_unit";
alter database "odk_unit" owner to "odk_unit";
\c "odk_unit";
create schema "odk_unit";
grant all privileges on schema "odk_unit" to "odk_unit";

