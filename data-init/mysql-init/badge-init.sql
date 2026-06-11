CREATE DATABASE IF NOT EXISTS badge_db;
CREATE USER IF NOT EXISTS 'user'@'%' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON badge_db.* TO 'user'@'%';
FLUSH PRIVILEGES;

USE badge_db;

CREATE TABLE IF NOT EXISTS badge (
    id_badge BIGINT NOT NULL AUTO_INCREMENT,
    id_porteur BIGINT NULL,
    statut VARCHAR(50) NOT NULL,
    date_creation DATETIME(6),
    date_association DATETIME(6),
    PRIMARY KEY (id_badge),
    KEY idx_badge_id_porteur (id_porteur)
);

INSERT INTO badge(id_badge, id_porteur, statut, date_creation, date_association)
VALUES
    (1, 5,    'ASSOCIE',    NOW(6), NOW(6)),
    (2, 6,    'ASSOCIE',    NOW(6), NOW(6)),
    (3, 7,    'ASSOCIE',    NOW(6), NOW(6)),
    (4, 8,    'ASSOCIE',    NOW(6), NOW(6)),
    (5, 9,    'ASSOCIE',    NOW(6), NOW(6)),
    (6, NULL, 'DISPONIBLE', NOW(6), NULL),
    (7, NULL, 'PERDU',      NOW(6), NULL),
    (8, NULL, 'DESACTIVE',  NOW(6), NULL);
