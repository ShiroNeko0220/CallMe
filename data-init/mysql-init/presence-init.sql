CREATE DATABASE IF NOT EXISTS presence_db;
CREATE USER IF NOT EXISTS 'user'@'%' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON presence_db.* TO 'user'@'%';
FLUSH PRIVILEGES;

USE presence_db;

CREATE TABLE IF NOT EXISTS presence (
    id_presence BIGINT NOT NULL AUTO_INCREMENT,
    id_badge BIGINT NOT NULL,
    id_porteur BIGINT NOT NULL,
    id_cours BIGINT NOT NULL,
    date_badgeage DATETIME(6) NOT NULL,
    PRIMARY KEY (id_presence),
    UNIQUE KEY uk_presence_porteur_cours (id_porteur, id_cours),
    KEY idx_presence_cours (id_cours),
    KEY idx_presence_porteur (id_porteur)
);

INSERT INTO presence(id_presence, id_badge, id_porteur, id_cours, date_badgeage)
VALUES
    (1, 1, 5, 1, NOW(6)),
    (2, 2, 6, 2, NOW(6)),
    (3, 3, 7, 3, NOW(6)),
    (4, 4, 8, 4, NOW(6)),
    (5, 5, 9, 5, NOW(6));
