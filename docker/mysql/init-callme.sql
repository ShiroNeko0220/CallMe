CREATE DATABASE IF NOT EXISTS utilisateur_db;
CREATE DATABASE IF NOT EXISTS cours_db;
CREATE DATABASE IF NOT EXISTS competition_db;
CREATE DATABASE IF NOT EXISTS badge_db;
CREATE DATABASE IF NOT EXISTS presence_db;
CREATE DATABASE IF NOT EXISTS statistiques_db;

CREATE USER IF NOT EXISTS 'user'@'%' IDENTIFIED BY 'password';

GRANT ALL PRIVILEGES ON utilisateur_db.* TO 'user'@'%';
GRANT ALL PRIVILEGES ON cours_db.* TO 'user'@'%';
GRANT ALL PRIVILEGES ON competition_db.* TO 'user'@'%';
GRANT ALL PRIVILEGES ON badge_db.* TO 'user'@'%';
GRANT ALL PRIVILEGES ON presence_db.* TO 'user'@'%';
GRANT ALL PRIVILEGES ON statistiques_db.* TO 'user'@'%';

FLUSH PRIVILEGES;
