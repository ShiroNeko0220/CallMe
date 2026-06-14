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

-- Présences sur cours futurs (1-5) — un membre par cours pour démonstration du scan
INSERT INTO presence(id_presence, id_badge, id_porteur, id_cours, date_badgeage)
VALUES
    (1, 1, 5, 1, NOW(6)),
    (2, 2, 6, 2, NOW(6)),
    (3, 3, 7, 3, NOW(6)),
    (4, 4, 8, 4, NOW(6)),
    (5, 5, 9, 5, NOW(6)),

-- Présences historiques sur cours passés (6-10) — plusieurs membres par cours pour des stats riches
-- Cours 6 (Salsa niv 1) : membres 5 et 6
    (6,  1, 5,  6, DATE_ADD(DATE_SUB(NOW(6), INTERVAL 60 DAY), INTERVAL 10 HOUR)),
    (7,  2, 6,  6, DATE_ADD(DATE_SUB(NOW(6), INTERVAL 60 DAY), INTERVAL 10 HOUR)),

-- Cours 7 (Bachata niv 2) : membres 6, 7, 10
    (8,  2, 6,  7, DATE_ADD(DATE_SUB(NOW(6), INTERVAL 45 DAY), INTERVAL 14 HOUR)),
    (9,  3, 7,  7, DATE_ADD(DATE_SUB(NOW(6), INTERVAL 45 DAY), INTERVAL 14 HOUR)),
    (10, 9, 10, 7, DATE_ADD(DATE_SUB(NOW(6), INTERVAL 45 DAY), INTERVAL 14 HOUR)),

-- Cours 8 (Tango niv 3) : membres 7, 9, 10
    (11, 3, 7,  8, DATE_ADD(DATE_SUB(NOW(6), INTERVAL 30 DAY), INTERVAL 16 HOUR)),
    (12, 5, 9,  8, DATE_ADD(DATE_SUB(NOW(6), INTERVAL 30 DAY), INTERVAL 16 HOUR)),
    (13, 9, 10, 8, DATE_ADD(DATE_SUB(NOW(6), INTERVAL 30 DAY), INTERVAL 16 HOUR)),

-- Cours 9 (Rumba niv 4) : membres 8, 9
    (14, 4, 8, 9, DATE_ADD(DATE_SUB(NOW(6), INTERVAL 20 DAY), INTERVAL 18 HOUR)),
    (15, 5, 9, 9, DATE_ADD(DATE_SUB(NOW(6), INTERVAL 20 DAY), INTERVAL 18 HOUR)),

-- Cours 10 (Flamenco niv 5) : membre 9
    (16, 5, 9, 10, DATE_ADD(DATE_SUB(NOW(6), INTERVAL 10 DAY), INTERVAL 19 HOUR));
