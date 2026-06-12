CREATE DATABASE IF NOT EXISTS statistiques_db;
CREATE USER IF NOT EXISTS 'user'@'%' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON statistiques_db.* TO 'user'@'%';
FLUSH PRIVILEGES;

USE statistiques_db;

CREATE TABLE IF NOT EXISTS rapport_statistique (
    id BIGINT NOT NULL AUTO_INCREMENT,
    type_rapport VARCHAR(100) NOT NULL,
    valeur TEXT NOT NULL,
    date_creation DATETIME NOT NULL,
    PRIMARY KEY (id)
);
