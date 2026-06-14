# Feuille de route — Tests fonctionnels & techniques

> **Comptes de test** — mot de passe universel : `password`
>
> | Login | Rôle | Niveau |
> |---|---|---|
> | `presidente` | PRESIDENT | 5 |
> | `secretaire` | SECRETAIRE | 4 |
> | `enseignant3` | ENSEIGNANT | 3 |
> | `enseignant5` | ENSEIGNANT | 5 |
> | `membre1` | MEMBRE | 1 |
> | `membre3` | MEMBRE | 3 |
> | `membre3b` | MEMBRE | 3 |

---

## Authentification

| # | Action | Rôle requis | Données de test | Résultat attendu |
|---|--------|-------------|-----------------|------------------|
| A-01 | Connexion valide | — | `presidente` / `password` | Accès tableau de bord, nom affiché en haut |
| A-02 | Connexion identifiant inconnu | — | `inconnu` / `password` | Message d'erreur "identifiant ou mot de passe incorrect" |
| A-03 | Connexion mauvais mot de passe | — | `presidente` / `wrong` | Message d'erreur |
| A-04 | Afficher/masquer le mot de passe | — | Cliquer l'œil dans le champ mdp | Bascule entre `••••` et texte clair |
| A-05 | Déconnexion | Tout | Cliquer "Déconnexion" | Retour page login, session effacée |
| A-06 | Persistance session (refresh) | Tout | F5 après connexion | Reste connecté avec le même compte |

---

## Navigation & accès par rôle

| # | Action | Rôle | Résultat attendu |
|---|--------|------|------------------|
| N-01 | Menu MEMBRE | MEMBRE | Visible : Cours, Compétitions, Présences |
| N-02 | Menu ENSEIGNANT | ENSEIGNANT | Visible : Membres, Cours, Compétitions, Présences |
| N-03 | Menu SECRETAIRE | SECRETAIRE | Visible : Membres, Cours, Compétitions, Badges, Présences |
| N-04 | Menu PRESIDENT | PRESIDENT | Visible : tout (+ Statistiques) |
| N-05 | Onglet Badges absent | MEMBRE / ENSEIGNANT | Onglet "Badges" non affiché |
| N-06 | Onglet Statistiques absent | MEMBRE / SECRETAIRE / ENSEIGNANT | Onglet "Statistiques" non affiché |

---

## Membres

| # | Action | Rôle | Données de test | Résultat attendu |
|---|--------|------|-----------------|------------------|
| M-01 | Lister les membres | ENSEIGNANT / SECRETAIRE / PRESIDENT | — | Tableau avec les 10 membres |
| M-02 | Lister les membres interdit | MEMBRE | — | Onglet "Membres" absent du menu |
| M-03 | Créer un membre | SECRETAIRE | Nom : Martin, Prénom : Lucie, Email : lucie@test.fr, Login : lucie, Mdp : password, Ville : Lyon | Membre créé avec rôle MEMBRE et niveau 1 |
| M-04 | Créer — login déjà utilisé | SECRETAIRE | Login : `presidente` | Erreur "Utilisateur existant" |
| M-05 | Créer — bouton absent | MEMBRE / ENSEIGNANT | — | Bouton "+ Nouveau membre" non affiché |
| M-06 | Supprimer un membre | PRESIDENT | Supprimer le membre créé en M-03 | Confirmation modale, membre retiré de la liste |
| M-07 | Supprimer — bouton absent | SECRETAIRE / ENSEIGNANT / MEMBRE | — | Colonne "Action" absente du tableau |
| M-08 | Afficher/masquer mdp à la création | SECRETAIRE | Cliquer l'œil dans le champ mdp | Bascule texte clair / masqué |

---

## Cours

| # | Action | Rôle | Données de test | Résultat attendu |
|---|--------|------|-----------------|------------------|
| C-01 | Lister les cours | Tout | — | 10 cours (5 futurs + 5 passés) |
| C-02 | Filtrer par niveau | Tout | Cliquer "Niv. 3" | Seulement les cours niveau 3 |
| C-03 | Créer un cours | SECRETAIRE | Titre : Zumba niv1, Date : +8 jours, Heure : 10:00, Durée : 60, Lieu : Salle A, Enseignant : Anh Nguyen (Niv. 3), Niveau : 1 | Cours créé et visible dans la liste |
| C-04 | Créer — enseignant non apte | SECRETAIRE | Même données, Enseignant : Anh Nguyen (Niv. 3), Niveau : 5 | Erreur "enseignant non apte" |
| C-05 | Créer — date trop proche | SECRETAIRE | Date : demain | Erreur date invalide |
| C-06 | Créer — durée minimale | SECRETAIRE | Durée : 10 (taper au clavier) | La valeur est bloquée à 45 min minimum |
| C-07 | Créer — bouton absent | MEMBRE / ENSEIGNANT | — | Bouton "+ Nouveau cours" non affiché |
| C-08 | Supprimer un cours | PRESIDENT | Supprimer le cours créé en C-03 | Confirmation modale, cours supprimé |
| C-09 | Supprimer — bouton absent | SECRETAIRE / ENSEIGNANT / MEMBRE | — | Bouton "Supprimer" absent de la liste |

---

## Compétitions

| # | Action | Rôle | Données de test | Résultat attendu |
|---|--------|------|-----------------|------------------|
| K-01 | Lister les compétitions | Tout | — | 5 compétitions affichées |
| K-02 | Voir les résultats | Tout | Cliquer "Tournoi chorégraphie niveau 3" | 2 résultats (membres 7 et 10) |
| K-03 | Créer une compétition | ENSEIGNANT | Titre : Open niv2, Date : +8j, Heure : 09:00, Durée : 90, Lieu : Gymnase, Enseignant : Anh Nguyen (Niv. 3), Niveau : 2 | Compétition créée |
| K-04 | Créer — enseignant non apte | ENSEIGNANT | Enseignant : Anh Nguyen (Niv. 3) pour niveau 5 | Erreur |
| K-05 | Créer — bouton absent | MEMBRE | — | Bouton "+ Nouvelle compétition" non affiché |
| K-06 | Ajouter un résultat | ENSEIGNANT | Compétition niv3, Membre : Peter Parker, Enseignant : Anh Nguyen, Note : 8.5 | Erreur 409 (résultat déjà existant pour ce membre) |
| K-07 | Ajouter un résultat nouveau | ENSEIGNANT | Compétition niv1, Membre : Doom Van, Enseignant : Anh Nguyen, Note : 9.0 | Erreur 409 (déjà existant) — tester avec une nouvelle compétition |
| K-08 | Ajouter — membre niveau incorrect | ENSEIGNANT | Compétition niv5, Membre : Doom Van (niv1) | Erreur 400 niveau incompatible |
| K-09 | Ajouter — bouton absent | MEMBRE | — | Bouton "+ Résultat" non affiché |
| K-10 | Supprimer une compétition | PRESIDENT | Supprimer la compétition créée en K-03 | Confirmation modale, compétition supprimée |
| K-11 | Supprimer — bouton absent | ENSEIGNANT / MEMBRE | — | Bouton "Supprimer" non affiché |

---

## Badges NFC

| # | Action | Rôle | Données de test | Résultat attendu |
|---|--------|------|-----------------|------------------|
| B-01 | Lister les badges | SECRETAIRE / PRESIDENT | — | 10 badges (5 associés, 2 disponibles, 1 perdu, 1 désactivé) |
| B-02 | Onglet absent | MEMBRE / ENSEIGNANT | — | Onglet "Badges" non affiché |
| B-03 | Créer un badge | SECRETAIRE | Cliquer "+ Créer un badge" | Badge créé avec statut DISPONIBLE |
| B-04 | Associer un badge | SECRETAIRE | Badge : 6, Membre : Oliver Queen (membre3b) | Erreur 409 — ce membre a déjà le badge 9 |
| B-05 | Associer badge disponible | SECRETAIRE | Badge : 10, Membre : TestN Test (presidente) | Badge associé, statut passe à ASSOCIE |
| B-06 | Associer badge déjà pris | SECRETAIRE | Badge : 1 (déjà associé au membre Doom Van) | Erreur 409 badge déjà associé |
| B-07 | Dissocier un badge | SECRETAIRE | Dissocier badge 10 (associé en B-05) | Confirmation modale, badge repasse DISPONIBLE |
| B-08 | Formulaire absent | MEMBRE / ENSEIGNANT | — | Formulaire "Associer" et colonne "Actions" non affichés |

---

## Présences & Badgeage

| # | Action | Rôle | Données de test | Résultat attendu |
|---|--------|------|-----------------|------------------|
| P-01 | Scanner un badge | Tout | Badge : 1, Cours : Danse niveau 2 | Présence enregistrée (badge 1 non encore scanné pour ce cours) |
| P-02 | Scanner — doublon | Tout | Badge : 1, Cours : Danse niveau 1 (déjà présent) | Erreur 409 "déjà scanné" |
| P-03 | Scanner — badge inexistant | Tout | Badge : 999, Cours : Danse niveau 1 | Erreur 404 badge introuvable |
| P-04 | Consulter par cours | ENSEIGNANT / SECRETAIRE / PRESIDENT | Cours : Tango niveau 3 | 3 présences (Peter Parker, Allen Barry, Oliver Queen) |
| P-05 | Consulter par membre | ENSEIGNANT / SECRETAIRE / PRESIDENT | Membre : Allen Barry (membre5) | Présences aux cours Danse niveau 5, Tango niveau 3, Rumba avancée, Flamenco expert |
| P-06 | Consulter — section absente | MEMBRE | — | Section "Consulter les présences" non affichée |

---

## Statistiques

| # | Action | Rôle | Données de test | Résultat attendu |
|---|--------|------|-----------------|------------------|
| S-01 | Accès statistiques | PRESIDENT | Connexion `presidente` | Onglet "Statistiques" visible et accessible |
| S-02 | Accès interdit | MEMBRE / ENSEIGNANT / SECRETAIRE | — | Onglet "Statistiques" absent du menu |
| S-03 | Chargement auto — activité cours | PRESIDENT | Ouvrir la page Statistiques | "X cours organisés" + moyenne élèves présents/cours |
| S-04 | Nb compétitions par niveau | PRESIDENT | Ouvrir la page Statistiques | Histogramme niveaux 1 à 5 avec nombre par barre |
| S-05 | Élèves présents à un cours | PRESIDENT | Sélectionner "Tango niveau 3" dans la liste déroulante + cliquer Afficher | Tableau : 3 élèves (Peter Parker, Allen Barry, Oliver Queen) avec badge et date de scan |
| S-06 | Parcours membre — cours | PRESIDENT | Sélectionner "Allen Barry (membre5)" dans "Parcours d'un membre" + cliquer Afficher | Colonne Cours : 4 cours, tous Présent |
| S-07 | Parcours membre — compétitions | PRESIDENT | Même sélection membre Allen Barry | Colonne Compétitions : 1 résultat "Grand prix niveau 5" — 9.2/10 |
| S-08 | Parcours avec filtre période | PRESIDENT | Membre : Allen Barry, Du : il y a 35 jours, Au : aujourd'hui | Seulement les cours dans la période (date de filtre incluse) |
| S-09 | Filtre période sans résultat | PRESIDENT | Membre : Allen Barry, Du : 1900-01-01, Au : 1900-01-02 | Les deux colonnes affichent "Aucun cours / compétition sur cette période" |

---

## Tests techniques

| # | Test | Méthode | Résultat attendu |
|---|------|---------|------------------|
| T-01 | Tous les microservices démarrés | `docker compose ps` | 12 conteneurs `Up` (frontend exclu — lancé séparément avec `npm run dev`) |
| T-02 | Eureka — services enregistrés | http://localhost:8761 | 6 MS visibles (utilisateur, cours, competition, badge, presence, statistiques) |
| T-03 | RabbitMQ — queues actives | http://localhost:15672 (guest/guest) | Queues `statistiques.*` présentes |
| T-04 | Gateway routing | `GET /api/cours` via http://localhost:8080 | Réponse 200 avec liste des cours |
| T-05 | Sécurité — endpoint sans rôle | `GET /api/utilisateurs` sans header X-Role | 403 Forbidden |
| T-06 | Sécurité — mauvais rôle | `POST /api/cours` avec header X-Role: MEMBRE | 403 Forbidden |
| T-07 | Event RabbitMQ — création cours | Créer un cours via l'UI | stat_cours mis à jour dans statistiques_db |
| T-08 | Event RabbitMQ — scan badge | Scanner un badge via l'UI | stat_presence mis à jour dans statistiques_db |
| T-09 | Tests unitaires | `mvn test` dans chaque MS | BUILD SUCCESS sur tous les modules |
| T-10 | Prometheus métriques | http://localhost:9090 | Métriques des MS disponibles |
| T-11 | Zipkin traces | http://localhost:9411 | Traces des appels inter-services visibles |
| T-12 | Persistance données | `docker compose restart` (sans `-v`) | Données toujours présentes après redémarrage |
