CREATE DATABASE IF NOT EXISTS cours_db;
CREATE USER IF NOT EXISTS 'user'@'%' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON cours_db.* TO 'user'@'%';
FLUSH PRIVILEGES;

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

-- Cours futurs (pour tester la création / inscription)
INSERT INTO cours(id, titre, date, heure_debut, duree_minutes, lieu, niveau_cible, enseignant_id)
VALUES
    (1, 'Danse niveau 1',  DATE_ADD(CURDATE(), INTERVAL 10 DAY), '10:00:00', 60,  'Salle A',     1, 3),
    (2, 'Danse niveau 2',  DATE_ADD(CURDATE(), INTERVAL 11 DAY), '14:00:00', 75,  'Salle B',     2, 3),
    (3, 'Danse niveau 3',  DATE_ADD(CURDATE(), INTERVAL 12 DAY), '16:00:00', 90,  'Salle C',     3, 3),
    (4, 'Danse niveau 4',  DATE_ADD(CURDATE(), INTERVAL 13 DAY), '18:00:00', 90,  'Salle D',     4, 4),
    (5, 'Danse niveau 5',  DATE_ADD(CURDATE(), INTERVAL 14 DAY), '19:00:00', 120, 'Grande Salle',5, 4),

-- Cours passés (pour historique de présences et statistiques)
    (6,  'Salsa débutant',      DATE_SUB(CURDATE(), INTERVAL 60 DAY), '10:00:00', 60,  'Salle A',     1, 3),
    (7,  'Bachata intermédiaire',DATE_SUB(CURDATE(), INTERVAL 45 DAY), '14:00:00', 75,  'Salle B',     2, 3),
    (8,  'Tango niveau 3',      DATE_SUB(CURDATE(), INTERVAL 30 DAY), '16:00:00', 90,  'Salle C',     3, 3),
    (9,  'Rumba avancée',       DATE_SUB(CURDATE(), INTERVAL 20 DAY), '18:00:00', 90,  'Salle D',     4, 4),
    (10, 'Flamenco expert',     DATE_SUB(CURDATE(), INTERVAL 10 DAY), '19:00:00', 120, 'Grande Salle',5, 4);
