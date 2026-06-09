CREATE DATABASE IF NOT EXISTS utilisateur_db;
CREATE DATABASE IF NOT EXISTS cours_db;
CREATE DATABASE IF NOT EXISTS presence_db;
CREATE DATABASE IF NOT EXISTS badge_db;
CREATE DATABASE IF NOT EXISTS statistique_db;

CREATE USER IF NOT EXISTS 'user'@'%' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON utilisateur_db.* TO 'user'@'%';
GRANT ALL PRIVILEGES ON cours_db.* TO 'user'@'%';
GRANT ALL PRIVILEGES ON presence_db.* TO 'user'@'%';
GRANT ALL PRIVILEGES ON badge_db.* TO 'user'@'%';
GRANT ALL PRIVILEGES ON statistique_db.* TO 'user'@'%';
FLUSH PRIVILEGES;

-- ---------------------------------------------------------------------------
-- Utilisateur
-- ---------------------------------------------------------------------------
USE utilisateur_db;

CREATE TABLE IF NOT EXISTS utilisateur (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nom VARCHAR(255) NOT NULL,
    prenom VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    login VARCHAR(255) NOT NULL,
    mot_de_passe VARCHAR(255) NOT NULL,
    ville VARCHAR(255),
    pays VARCHAR(255),
    niveau_expertise INT NOT NULL,
    role VARCHAR(50),
    PRIMARY KEY (id),
    UNIQUE KEY uk_utilisateur_email (email),
    UNIQUE KEY uk_utilisateur_login (login)
    );

INSERT INTO utilisateur(id, nom, prenom, email, login, mot_de_passe, ville, pays, niveau_expertise, role)
VALUES
    (1,  'Martin',  'Claire',  'presidente@callme.fr',   'presidente',  'password', 'Toulouse',    'France', 5, 'PRESIDENT'),
    (2,  'Durand',  'Sophie',  'secretaire@callme.fr',   'secretaire',  'password', 'Toulouse',    'France', 4, 'SECRETAIRE'),
    (3,  'Nguyen',  'Anh',     'enseignant3@callme.fr',  'enseignant3', 'password', 'Ramonville',  'France', 3, 'ENSEIGNANT'),
    (4,  'Bernard', 'Luc',     'enseignant5@callme.fr',  'enseignant5', 'password', 'Blagnac',     'France', 5, 'ENSEIGNANT'),
    (5,  'Petit',   'Emma',    'membre1@callme.fr',      'membre1',     'password', 'Toulouse',    'France', 1, 'MEMBRE'),
    (6,  'Robert',  'Noah',    'membre2@callme.fr',      'membre2',     'password', 'Colomiers',   'France', 2, 'MEMBRE'),
    (7,  'Moreau',  'Lina',    'membre3@callme.fr',      'membre3',     'password', 'Toulouse',    'France', 3, 'MEMBRE'),
    (8,  'Simon',   'Hugo',    'membre4@callme.fr',      'membre4',     'password', 'Muret',       'France', 4, 'MEMBRE'),
    (9,  'Garcia',  'Mila',    'membre5@callme.fr',      'membre5',     'password', 'Balma',       'France', 5, 'MEMBRE'),
    (10, 'Dubois',  'Jade',    'membre3b@callme.fr',     'membre3b',    'password', 'Toulouse',    'France', 3, 'MEMBRE');

-- ---------------------------------------------------------------------------
-- COURS
-- ---------------------------------------------------------------------------
USE cours_db;

CREATE TABLE IF NOT EXISTS cours (
    id BIGINT NOT NULL AUTO_INCREMENT,
    titre VARCHAR(255) NOT NULL,
    date DATE NOT NULL,
    heure_debut TIME NOT NULL,
    duree_minutes INT NOT NULL,
    lieu VARCHAR(255) NOT NULL,
    niveau_cible INT NOT NULL,
    enseignant_id BIGINT NOT NULL,
    PRIMARY KEY (id)
    );

INSERT INTO cours(id, titre, date, heure_debut, duree_minutes, lieu, niveau_cible, enseignant_id)
VALUES
    (1, 'Initiation rythmique niveau 1', DATE_ADD(CURDATE(), INTERVAL 10 DAY), '10:00:00', 60, 'Salle A', 1, 3),
    (2, 'Technique niveau 2',           DATE_ADD(CURDATE(), INTERVAL 11 DAY), '14:00:00', 75, 'Salle B', 2, 3),
    (3, 'Chorégraphie niveau 3',        DATE_ADD(CURDATE(), INTERVAL 12 DAY), '16:00:00', 90, 'Salle C', 3, 3),
    (4, 'Préparation avancée niveau 4', DATE_ADD(CURDATE(), INTERVAL 13 DAY), '18:00:00', 90, 'Salle D', 4, 4),
    (5, 'Masterclass niveau 5',         DATE_ADD(CURDATE(), INTERVAL 14 DAY), '19:00:00', 120,'Grande Salle', 5, 4);

-- ---------------------------------------------------------------------------
-- BADGE
-- ---------------------------------------------------------------------------
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

-- ---------------------------------------------------------------------------
-- PRESENCE
-- ---------------------------------------------------------------------------
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
