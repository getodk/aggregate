create database `odk_unit`;
create user 'odk_unit'@'localhost' identified by 'odk_unit';
grant all on `odk_unit`.* to 'odk_unit'@'localhost' identified by 'odk_unit';
flush privileges;
