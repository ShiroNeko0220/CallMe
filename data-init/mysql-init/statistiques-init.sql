CREATE DATABASE IF NOT EXISTS statistiques_db;
CREATE USER IF NOT EXISTS 'user'@'%' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON statistiques_db.* TO 'user'@'%';
FLUSH PRIVILEGES;

USE statistiques_db;

-- Tables miroir alimentées par RabbitMQ (pré-remplies pour démo immédiate)

CREATE TABLE IF NOT EXISTS stat_cours (
    id BIGINT NOT NULL,
    titre VARCHAR(255),
    niveau_cible INT,
    date DATE,
    heure_debut TIME,
    duree INT,
    enseignant_id BIGINT,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS stat_presence (
    id BIGINT NOT NULL,
    id_porteur BIGINT,
    id_cours BIGINT,
    date_badgeage DATETIME,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS stat_competition (
    id VARCHAR(255) NOT NULL,
    titre VARCHAR(255),
    niveau_cible INT,
    date DATE,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS stat_resultat (
    id VARCHAR(255) NOT NULL,
    competition_id VARCHAR(255),
    eleve_id BIGINT,
    enseignant_id BIGINT,
    note DECIMAL(4,2),
    competition_date DATE,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS stat_eleve (
    id BIGINT NOT NULL,
    niveau_expertise INT,
    PRIMARY KEY (id)
);

-- Membres (IDs 5-10)
INSERT INTO stat_eleve(id, niveau_expertise) VALUES
    (5,  1),
    (6,  2),
    (7,  3),
    (8,  4),
    (9,  5),
    (10, 3);

-- Cours passés (6-10) utilisés pour les statistiques
INSERT INTO stat_cours(id, titre, niveau_cible, date, heure_debut, duree, enseignant_id) VALUES
    (1,  'Danse niveau 1',       1, DATE_ADD(CURDATE(), INTERVAL 10 DAY),  '10:00:00', 60,  3),
    (2,  'Danse niveau 2',       2, DATE_ADD(CURDATE(), INTERVAL 11 DAY),  '14:00:00', 75,  3),
    (3,  'Danse niveau 3',       3, DATE_ADD(CURDATE(), INTERVAL 12 DAY),  '16:00:00', 90,  3),
    (4,  'Danse niveau 4',       4, DATE_ADD(CURDATE(), INTERVAL 13 DAY),  '18:00:00', 90,  4),
    (5,  'Danse niveau 5',       5, DATE_ADD(CURDATE(), INTERVAL 14 DAY),  '19:00:00', 120, 4),
    (6,  'Salsa débutant',       1, DATE_SUB(CURDATE(), INTERVAL 60 DAY),  '10:00:00', 60,  3),
    (7,  'Bachata intermédiaire',2, DATE_SUB(CURDATE(), INTERVAL 45 DAY),  '14:00:00', 75,  3),
    (8,  'Tango niveau 3',       3, DATE_SUB(CURDATE(), INTERVAL 30 DAY),  '16:00:00', 90,  3),
    (9,  'Rumba avancée',        4, DATE_SUB(CURDATE(), INTERVAL 20 DAY),  '18:00:00', 90,  4),
    (10, 'Flamenco expert',      5, DATE_SUB(CURDATE(), INTERVAL 10 DAY),  '19:00:00', 120, 4);

-- Présences (miroir de presence-init.sql)
INSERT INTO stat_presence(id, id_porteur, id_cours, date_badgeage) VALUES
    (1,  5,  1,  NOW()),
    (2,  6,  2,  NOW()),
    (3,  7,  3,  NOW()),
    (4,  8,  4,  NOW()),
    (5,  9,  5,  NOW()),
    (6,  5,  6,  DATE_ADD(DATE_SUB(NOW(), INTERVAL 60 DAY), INTERVAL 10 HOUR)),
    (7,  6,  6,  DATE_ADD(DATE_SUB(NOW(), INTERVAL 60 DAY), INTERVAL 10 HOUR)),
    (8,  6,  7,  DATE_ADD(DATE_SUB(NOW(), INTERVAL 45 DAY), INTERVAL 14 HOUR)),
    (9,  7,  7,  DATE_ADD(DATE_SUB(NOW(), INTERVAL 45 DAY), INTERVAL 14 HOUR)),
    (10, 10, 7,  DATE_ADD(DATE_SUB(NOW(), INTERVAL 45 DAY), INTERVAL 14 HOUR)),
    (11, 7,  8,  DATE_ADD(DATE_SUB(NOW(), INTERVAL 30 DAY), INTERVAL 16 HOUR)),
    (12, 9,  8,  DATE_ADD(DATE_SUB(NOW(), INTERVAL 30 DAY), INTERVAL 16 HOUR)),
    (13, 10, 8,  DATE_ADD(DATE_SUB(NOW(), INTERVAL 30 DAY), INTERVAL 16 HOUR)),
    (14, 8,  9,  DATE_ADD(DATE_SUB(NOW(), INTERVAL 20 DAY), INTERVAL 18 HOUR)),
    (15, 9,  9,  DATE_ADD(DATE_SUB(NOW(), INTERVAL 20 DAY), INTERVAL 18 HOUR)),
    (16, 9,  10, DATE_ADD(DATE_SUB(NOW(), INTERVAL 10 DAY), INTERVAL 19 HOUR));

-- Compétitions (miroir de MongoDB)
INSERT INTO stat_competition(id, titre, niveau_cible, date) VALUES
    ('comp-n1-initiation',    'Competition decouverte niveau 1', 1, DATE_ADD(CURDATE(), INTERVAL 20 DAY)),
    ('comp-n2-technique',     'Challenge technique niveau 2',    2, DATE_ADD(CURDATE(), INTERVAL 21 DAY)),
    ('comp-n3-choregraphie',  'Tournoi choregraphie niveau 3',   3, DATE_ADD(CURDATE(), INTERVAL 22 DAY)),
    ('comp-n4-avance',        'Open avance niveau 4',            4, DATE_ADD(CURDATE(), INTERVAL 23 DAY)),
    ('comp-n5-masterclass',   'Grand prix niveau 5',             5, DATE_ADD(CURDATE(), INTERVAL 24 DAY));

-- Résultats (miroir de MongoDB)
INSERT INTO stat_resultat(id, competition_id, eleve_id, enseignant_id, note, competition_date) VALUES
    ('res-comp-n1-membre5',  'comp-n1-initiation',   5,  3, 7.5, DATE_ADD(CURDATE(), INTERVAL 20 DAY)),
    ('res-comp-n2-membre6',  'comp-n2-technique',    6,  3, 8.0, DATE_ADD(CURDATE(), INTERVAL 21 DAY)),
    ('res-comp-n3-membre7',  'comp-n3-choregraphie', 7,  3, 8.7, DATE_ADD(CURDATE(), INTERVAL 22 DAY)),
    ('res-comp-n3-membre10', 'comp-n3-choregraphie', 10, 3, 7.5, DATE_ADD(CURDATE(), INTERVAL 22 DAY)),
    ('res-comp-n4-membre8',  'comp-n4-avance',       8,  4, 6.9, DATE_ADD(CURDATE(), INTERVAL 23 DAY)),
    ('res-comp-n5-membre9',  'comp-n5-masterclass',  9,  4, 9.2, DATE_ADD(CURDATE(), INTERVAL 24 DAY));
