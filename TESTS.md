# Feuille de route — Tests fonctionnels & techniques

> **Comptes de test** — mot de passe universel : `password`
>
> | Login | Rôle | Niveau | Badge |
> |---|---|---|---|
> | `presidente` | PRESIDENT | 5 | — |
> | `secretaire` | SECRETAIRE | 4 | — |
> | `enseignant3` | ENSEIGNANT | 3 | — |
> | `enseignant5` | ENSEIGNANT | 5 | — |
> | `membre1` | MEMBRE | 1 | Badge #1 (ASSOCIE) |
> | `membre2` | MEMBRE | 2 | Badge #2 (ASSOCIE) |
> | `membre3` | MEMBRE | 3 | Badge #3 (ASSOCIE) |
> | `membre4` | MEMBRE | 4 | Badge #4 (ASSOCIE) |
> | `membre5` | MEMBRE | 5 | Badge #5 (ASSOCIE) |
> | `membre3b` | MEMBRE | 3 | Badge #7 (ASSOCIE) |
>
> **Badges disponibles au démarrage :** #6 et #8 (statut DISPONIBLE, aucun porteur)

---

## Authentification & Création de compte

| # | Action | Rôle | Données de test | Résultat attendu |
|---|--------|------|-----------------|------------------|
| A-01 | Connexion valide | — | `presidente` / `password` | Accès tableau de bord, prénom affiché en haut à droite |
| A-02 | Connexion — identifiant inconnu | — | `inconnu` / `password` | Message d'erreur "identifiant ou mot de passe incorrect" |
| A-03 | Connexion — mauvais mot de passe | — | `presidente` / `wrong` | Message d'erreur |
| A-04 | Afficher/masquer le mot de passe | — | Cliquer l'œil dans le champ mdp | Bascule entre `••••` et texte clair |
| A-05 | Déconnexion | Tout | Cliquer "Déconnexion" | Retour page login, session effacée |
| A-06 | Persistance session (refresh F5) | Tout | F5 après connexion | Reste connecté avec le même compte |
| A-07 | Créer un compte — bouton | — | Cliquer "+ Nouveau compte" sur la page login | Formulaire de création s'ouvre en modal |
| A-08 | Créer un compte — succès | — | Nom : Test, Prénom : Jean, Email : jean@test.fr, Login : jean, Mdp : secret, Ville : Lyon | Modal de succès avec le login créé, bouton "Me connecter" |
| A-09 | Créer un compte — login déjà pris | — | Login : `presidente` | Erreur visible dans le formulaire |
| A-10 | Après création — pré-remplissage login | — | Valider la modal de succès | Champ login pré-rempli avec le nouveau login sur la page de connexion |

---

## Navigation & accès par rôle

| # | Action | Rôle | Résultat attendu |
|---|--------|------|------------------|
| N-01 | Menu MEMBRE | MEMBRE | Visible : Cours, Compétitions, Présences, Mon profil |
| N-02 | Menu ENSEIGNANT | ENSEIGNANT | Visible : Mes élèves, Cours, Compétitions, Présences, Mon profil |
| N-03 | Menu SECRETAIRE | SECRETAIRE | Visible : Membres, Cours, Compétitions, Badges, Présences |
| N-04 | Menu PRESIDENT | PRESIDENT | Visible : Membres, Cours, Compétitions, Badges, Présences, Statistiques |
| N-05 | Onglet Badges absent | MEMBRE / ENSEIGNANT | Onglet "Badges" non affiché |
| N-06 | Onglet Statistiques absent | MEMBRE / ENSEIGNANT / SECRETAIRE | Onglet "Statistiques" non affiché |
| N-07 | Onglet Membres absent | MEMBRE / ENSEIGNANT | Onglet "Membres" non affiché |
| N-08 | Mon profil absent | SECRETAIRE / PRESIDENT | Onglet "Mon profil" non affiché |

---

## Membres

| # | Action | Rôle | Données de test | Résultat attendu |
|---|--------|------|-----------------|------------------|
| M-01 | Lister les membres | SECRETAIRE / PRESIDENT | — | Tableau avec les 10 membres |
| M-02 | Lister les membres | ENSEIGNANT | — | Onglet "Membres" absent, "Mes élèves" à la place |
| M-03 | Créer un membre | SECRETAIRE | Nom : Martin, Prénom : Lucie, Email : lucie@test.fr, Login : lucie, Mdp : password, Ville : Lyon | Membre créé avec rôle MEMBRE et niveau 1 par défaut |
| M-04 | Créer — login déjà utilisé | SECRETAIRE | Login : `presidente` | Erreur "utilisateur existant" |
| M-05 | Créer — bouton absent | MEMBRE / ENSEIGNANT | — | Bouton "+ Nouveau membre" non affiché |
| M-06 | Modifier un membre (admin) | SECRETAIRE | Changer niveau de `lucie` à 3 et rôle à ENSEIGNANT | Modification enregistrée |
| M-07 | Modifier son profil | MEMBRE | Login `membre1` : changer la ville | Modification enregistrée sans pouvoir changer rôle/niveau |
| M-08 | Supprimer un membre | PRESIDENT | Supprimer le membre créé en M-03 | Confirmation modale, membre retiré de la liste |
| M-09 | Supprimer — bouton absent | SECRETAIRE / ENSEIGNANT / MEMBRE | — | Bouton "Supprimer" absent du tableau |

---

## Mes élèves (ENSEIGNANT)

| # | Action | Rôle | Données de test | Résultat attendu |
|---|--------|------|-----------------|------------------|
| E-01 | Accès section | ENSEIGNANT | Connexion `enseignant3` → onglet "Mes élèves" | Page visible uniquement pour ENSEIGNANT |
| E-02 | Liste des élèves | ENSEIGNANT | `enseignant3` (id=3) — enseigne les cours 1, 2, 3 | Membres ayant une présence à ces cours ou un résultat à ses compétitions |
| E-03 | Aucun élève | ENSEIGNANT | Compte enseignant sans cours ni compétition | Message "Aucun élève trouvé" |
| E-04 | Pas visible pour MEMBRE | MEMBRE | — | Onglet "Mes élèves" absent |

---

## Cours

| # | Action | Rôle | Données de test | Résultat attendu |
|---|--------|------|-----------------|------------------|
| C-01 | Lister les cours | Tout | — | 5 cours affichés |
| C-02 | Filtrer par niveau | Tout | Cliquer niveau "3" | Seulement les cours niveau 3 |
| C-03 | Créer un cours | SECRETAIRE | Titre : Zumba niv1, Date : +8 jours, Heure : 10:00, Durée : 60, Lieu : Salle A, Enseignant : Anh Nguyen (Niv. 3), Niveau : 1 | Cours créé et visible dans la liste |
| C-04 | Créer — enseignant non apte | SECRETAIRE | Enseignant : Anh Nguyen (Niv. 3), Niveau : 5 | Erreur "enseignant non apte" |
| C-05 | Créer — date trop proche | SECRETAIRE | Date : demain | Erreur "date doit être > 7 jours" |
| C-06 | Créer — durée minimale | SECRETAIRE | Durée : 10 min | Erreur ou valeur bloquée à 45 min minimum |
| C-07 | Créer — bouton absent | MEMBRE / ENSEIGNANT | — | Bouton "+ Nouveau cours" non affiché |
| C-08 | Supprimer un cours | PRESIDENT | Supprimer le cours créé en C-03 | Confirmation modale, cours supprimé |
| C-09 | Supprimer — bouton absent | SECRETAIRE / ENSEIGNANT / MEMBRE | — | Bouton "Supprimer" absent |

---

## Compétitions

| # | Action | Rôle | Données de test | Résultat attendu |
|---|--------|------|-----------------|------------------|
| K-01 | Lister les compétitions | Tout | — | Compétitions affichées |
| K-02 | Créer une compétition | ENSEIGNANT | Titre : Open niv2, Date : +8j, Heure : 09:00, Durée : 90, Lieu : Gymnase, Enseignant : Anh Nguyen (Niv. 3), Niveau : 2 | Compétition créée |
| K-03 | Créer — enseignant non apte | ENSEIGNANT | Enseignant : Anh Nguyen (Niv. 3) pour niveau 5 | Erreur |
| K-04 | Créer — MEMBRE interdit | MEMBRE | — | Bouton "+ Nouvelle compétition" non affiché |
| K-05 | Ajouter un résultat | ENSEIGNANT | Compétition niveau 3, Élève : membre3 (niv 3), Note : 7.5 | Résultat enregistré |
| K-06 | Résultat — doublon | ENSEIGNANT | Même élève sur même compétition | Erreur 409 |
| K-07 | Résultat — niveau incompatible | ENSEIGNANT | Compétition niv5, Élève : membre1 (niv1) | Erreur 400 |
| K-08 | Résultat — bouton absent | MEMBRE | — | Bouton "+ Résultat" non affiché |
| K-09 | Supprimer une compétition | PRESIDENT | Supprimer la compétition créée en K-02 | Confirmation modale, compétition supprimée |
| K-10 | Supprimer — bouton absent | ENSEIGNANT / MEMBRE | — | Bouton "Supprimer" non affiché |

---

## Badges NFC

| # | Action | Rôle | Données de test | Résultat attendu |
|---|--------|------|-----------------|------------------|
| B-01 | Lister les badges | SECRETAIRE / PRESIDENT | — | 8 badges : 6 ASSOCIE, 2 DISPONIBLE |
| B-02 | Onglet absent | MEMBRE / ENSEIGNANT | — | Onglet "Badges" non affiché |
| B-03 | Créer un badge | SECRETAIRE | Cliquer "+ Créer un badge" | Badge créé avec statut DISPONIBLE |
| B-04 | Associer un badge disponible | SECRETAIRE | Badge : 6, Membre : presidente (id=1) | Badge associé, statut ASSOCIE |
| B-05 | Associer — badge déjà associé | SECRETAIRE | Badge : 1 (déjà associé à membre1) | Erreur 409 "badge déjà associé" |
| B-06 | Associer — porteur déjà badgé | SECRETAIRE | Badge : 6, Membre : membre1 (id=5, badge #1 déjà) | Erreur 409 "porteur possède déjà un badge" |
| B-07 | Dissocier un badge | SECRETAIRE | Dissocier badge 6 (associé en B-04) | Confirmation modale, badge repasse DISPONIBLE |
| B-08 | Dissocier — PRESIDENT ne peut pas | PRESIDENT | Tenter de dissocier — bouton absent | Bouton "Dissocier" absent pour PRESIDENT |
| B-09 | Formulaire absent | MEMBRE / ENSEIGNANT | — | Section "Associer un badge" non affichée |
| B-10 | Alertes badges — enseignant sans badge | SECRETAIRE | Créer un cours avec enseignant3 (pas de badge associé) | Alerte amber apparaît dans BadgesView pour SECRETAIRE |
| B-11 | Alerte résolue — badge associé | SECRETAIRE | Associer badge #6 à enseignant3 (id=3) | L'alerte disparaît de la liste |

---

## Présences & Badgeage

| # | Action | Rôle | Données de test | Résultat attendu |
|---|--------|------|-----------------|------------------|
| P-01 | Auto-détection du badge | MEMBRE | Connexion `membre1` → onglet Présences | Bandeau vert "Badge #1 détecté" |
| P-02 | Pas de badge — avertissement | MEMBRE | Connexion compte sans badge associé | Bandeau orange "Aucun badge associé à votre compte" |
| P-03 | Cours disponibles filtrés | MEMBRE | Connexion `membre1` (niveau 1) | Seuls les cours de niveau 1 dans la liste |
| P-04 | Scanner un badge | MEMBRE | Badge : 1, Cours : sélectionner dans la liste | Présence enregistrée |
| P-05 | Scanner — doublon | MEMBRE | Rescanner badge 1 pour le même cours | Erreur 409 "déjà scanné" |
| P-06 | Scanner — badge inexistant | MEMBRE | Badge : 999, Cours : n'importe | Erreur 404 badge introuvable |
| P-07 | Scanner — badge DISPONIBLE | MEMBRE | Badge : 6 (DISPONIBLE), Cours : quelconque | Erreur (badge non associé) |
| P-08 | Consulter présences par cours | ENSEIGNANT / SECRETAIRE / PRESIDENT | Sélectionner un cours avec présences | Liste des membres présents |
| P-09 | Consulter présences par membre | ENSEIGNANT / SECRETAIRE / PRESIDENT | Sélectionner un membre | Liste des cours avec date de scan |
| P-10 | Scanner — section cachée | ENSEIGNANT / SECRETAIRE / PRESIDENT | — | Section scanner non affichée |

---

## Statistiques

| # | Action | Rôle | Données de test | Résultat attendu |
|---|--------|------|-----------------|------------------|
| S-01 | Accès statistiques | PRESIDENT | Connexion `presidente` | Onglet "Statistiques" visible |
| S-02 | Accès interdit | MEMBRE / ENSEIGNANT / SECRETAIRE | — | Onglet "Statistiques" absent du menu |
| S-03 | Vue d'ensemble — cours | PRESIDENT | Ouvrir statistiques | Nombre de cours + moyenne élèves présents |
| S-04 | Compétitions par niveau | PRESIDENT | Ouvrir statistiques | Histogramme niveaux 1 à 5 |
| S-05 | Élèves présents à un cours | PRESIDENT | Sélectionner un cours + cliquer Afficher | Tableau élèves avec badge et date de scan |
| S-06 | Parcours membre — cours | PRESIDENT | Sélectionner un membre | Liste des cours avec statut présent/absent |
| S-07 | Parcours membre — compétitions | PRESIDENT | Même sélection | Liste des compétitions avec notes |
| S-08 | Filtre période | PRESIDENT | Membre + date début + date fin | Données filtrées sur la période |
| S-09 | Filtre — aucun résultat | PRESIDENT | Période dans le passé lointain | "Aucun cours / compétition sur cette période" |

---

## Tests techniques & Architecture

| # | Test | Méthode | Résultat attendu |
|---|------|---------|------------------|
| T-01 | Tous les conteneurs démarrés | `docker compose ps` | 12+ conteneurs `Up` |
| T-02 | Eureka — services enregistrés | http://localhost:8761 | 6 MS visibles (utilisateur, cours, competition, badge, presence, statistiques) |
| T-03 | RabbitMQ — queues actives | http://localhost:15672 (`guest`/`guest`) | Queues `badge.cours.cree`, `badge.competition.creee`, `statistiques.*`, `presence.*` présentes |
| T-04 | Gateway routing | `GET /api/cours` via port 8080 | Réponse 200 avec liste des cours (pas de port direct) |
| T-05 | Sécurité — sans rôle | `GET /api/utilisateurs` sans header X-Role | 403 Forbidden |
| T-06 | Sécurité — mauvais rôle | `POST /api/cours` avec X-Role: MEMBRE | 403 Forbidden |
| T-07 | Event RabbitMQ — cours → alertes | Créer un cours (enseignant3 sans badge) via l'UI | Alerte badge visible dans BadgesView (SECRETAIRE) |
| T-08 | Event RabbitMQ — compétition → alertes | Créer une compétition (enseignant3 sans badge) | Alerte badge visible dans BadgesView |
| T-09 | Event RabbitMQ — alerte résolue | Associer badge à enseignant3 | Alerte disparaît de BadgesView |
| T-10 | Event RabbitMQ — cours → statistiques | Créer un cours via l'UI | stat_cours incrémenté dans statistiques_db |
| T-11 | Event RabbitMQ — présence → statistiques | Scanner un badge via l'UI | stat_presence mis à jour dans statistiques_db |
| T-12 | Tests unitaires | `mvn test` dans chaque MS | BUILD SUCCESS sur tous les modules |
| T-13 | Prometheus métriques | http://localhost:9090/targets | 6 MS + Eureka en statut UP |
| T-14 | Zipkin traces | http://localhost:9411 | Traces des appels inter-services visibles |
| T-15 | Circuit Breaker fallback Feign | Arrêter utilisateur-ms, créer un cours | Erreur 503 propre (circuit breaker) — pas de stacktrace |
| T-16 | statistiques-ms autonome | Arrêter cours-ms et competition-ms, consulter statistiques | Réponse 200 normale (statistiques-ms lit uniquement sa DB MongoDB locale) |
| T-17 | Persistance données | `docker compose restart` (sans `-v`) | Données toujours présentes après redémarrage |
| T-18 | Isolation des bases | Chaque MS a sa propre DB | 4 MySQL séparés + MongoDB pour competition et statistiques |
| T-19 | Config centralisée | Arrêter config-server après démarrage | MS fonctionnent (fallback local `application.yml`) |
| T-20 | RabbitMQ — aucun message en attente | http://localhost:15672 → Queues | `messages` = 0 sur toutes les queues (consommés) |