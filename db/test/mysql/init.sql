CREATE DATABASE aggregate;
CREATE USER aggregate@'%' IDENTIFIED BY 'aggregate';
GRANT ALL ON aggregate.* TO aggregate@'%' IDENTIFIED BY 'aggregate';
FLUSH PRIVILEGES;
