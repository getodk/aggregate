create database "odk_test";
create schema "odk_test";
create user "odk_test" with unencrypted password 'odk_test';
grant all privileges on database "odk_test" to "odk_test";
alter database "odk_test" owner to "odk_test";
