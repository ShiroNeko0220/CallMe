CREATE DATABASE IF NOT EXISTS utilisateur_db;
CREATE USER IF NOT EXISTS 'user'@'%' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON utilisateur_db.* TO 'user'@'%';
FLUSH PRIVILEGES;

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
    (1,  'Test',  'TestN',  'presidente@callme.fr',   'presidente',  'password', 'Toulouse',    'France', 5, 'PRESIDENT'),
    (2,  'Test',  'Sophie',  'secretaire@callme.fr',   'secretaire',  'password', 'Toulouse',    'France', 4, 'SECRETAIRE'),
    (3,  'Nguyen',  'Anh',     'enseignant3@callme.fr',  'enseignant3', 'password', 'Paris',  'France', 3, 'ENSEIGNANT'),
    (4,  'Banner', 'Bruce',     'enseignant5@callme.fr',  'enseignant5', 'password', 'Toulouse',     'France', 5, 'ENSEIGNANT'),
    (5,  'Van',   'Doom',    'membre1@callme.fr',      'membre1',     'password', 'Toulouse',    'France', 1, 'MEMBRE'),
    (6,  'Robert',  'DJ',    'membre2@callme.fr',      'membre2',     'password', 'Toulouse',   'France', 2, 'MEMBRE'),
    (7,  'Parker',  'Peter',    'membre3@callme.fr',      'membre3',     'password', 'Toulouse',    'France', 3, 'MEMBRE'),
    (8,  'Bruce',   'Wayne',    'membre4@callme.fr',      'membre4',     'password', 'Toulouse',       'France', 4, 'MEMBRE'),
    (9,  'Barry',  'Allen',    'membre5@callme.fr',      'membre5',     'password', 'Toulouse',       'France', 5, 'MEMBRE'),
    (10, 'Queen',  'Oliver',    'membre3b@callme.fr',     'membre3b',    'password', 'Toulouse',    'France', 3, 'MEMBRE');
