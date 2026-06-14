# Oduru  (CallMe)

**Binôme :** Ha Thu Dinh & Katia Bouarab  
**Module :** Architectures Microservices sur Cloud  
**Formation :** Master MIAGE M2 – ITN, Université Toulouse Capitole

---

## Présentation

Oduru est une application de gestion pour un club de danse rythmique. Elle couvre la gestion des membres, des cours, des compétitions, du badgeage NFC des présences et des statistiques du club.

L'application est découpée en **6 microservices métier** indépendants, exposés via une API Gateway, et pilotés depuis une interface web React.

---

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│  Frontend React/Vite (Nginx)                   :3000            │
└────────────────────────────┬────────────────────────────────────┘
                             │ HTTP
┌────────────────────────────▼────────────────────────────────────┐
│  API Gateway (Spring Cloud Gateway)            :8080            │
│  Circuit Breaker : Resilience4j                                 │
└──┬──────────┬──────────┬──────────┬──────────┬──────────┬───────┘
   │          │          │          │          │          │
   ▼          ▼          ▼          ▼          ▼          ▼
utilisateur  cours   competition  badge    presence  statistiques
  :8081      :8082     :8083      :8084     :8085      :8086
   │          │          │          │          │          │
 MySQL      MySQL     MongoDB    MySQL     MySQL    (Feign vers
 :3307      :3308     :27017     :3309     :3310    les autres)

Registre de services : Eureka                   :8761
Config centralisé   : Config Server             :8888
Messaging asynchrone : RabbitMQ                 :5672 / :15672
Tracing distribué   : Zipkin                    :9411
Métriques           : Prometheus                :9090
```

**Communication inter-services :**
- **Synchrone (Feign)** : statistiques-ms interroge tous les autres ; competition-ms et cours-ms vérifient les utilisateurs auprès de utilisateur-ms.
- **Asynchrone (RabbitMQ)** : cours-ms publie un événement `cours.*` sur l'exchange `callme` à chaque création de cours ; presence-ms écoute la queue correspondante.

---

## Config Server (Spring Cloud Config)

Le Config Server centralise toutes les configurations des microservices au démarrage. Il utilise le backend **native** (fichiers locaux) et s'enregistre auprès d'Eureka.

### Structure des fichiers de configuration

```
config-server/src/main/resources/config/
├── application.yml          ← Configuration partagée (Eureka, Zipkin, métriques)
├── utilisateur-ms.yml       ← Port 8081, MySQL utilisateur_db :3307
├── cours-ms.yml             ← Port 8082, MySQL cours_db :3308, RabbitMQ, Resilience4j
├── competition-ms.yml       ← Port 8083, MongoDB competition_db :27017, Resilience4j
├── badge-ms.yml             ← Port 8084, MySQL badge_db :3309
├── presence-ms.yml          ← Port 8085, MySQL presence_db :3310, RabbitMQ
├── statistiques-ms.yml      ← Port 8086, pas de base propre (agrégateur)
└── gateway.yml              ← Port 8080, routes Spring Cloud Gateway
```

Chaque microservice importe automatiquement sa configuration au démarrage via :
```yaml
spring:
  config:
    import: optional:configserver:${CONFIG_SERVER_URL:http://localhost:8888}
```

Le `optional:` garantit que le microservice démarre même si le Config Server est indisponible (fallback sur son `application.yml` local).

### API Config Server

| URL | Description |
|-----|-------------|
| `GET /cours-ms/default` | Configuration active de cours-ms |
| `GET /actuator/env` | Toutes les propriétés chargées |
| `POST /actuator/refresh` | Rechargement à chaud (avec `@RefreshScope`) |

---

## Microservices

### utilisateur-ms — Gestion des membres

Base : **MySQL**

| Méthode | Endpoint | Rôle requis | Description |
|---------|----------|-------------|-------------|
| POST | `/utilisateurs/login` | — | Authentification |
| POST | `/utilisateurs` | SECRETAIRE, PRESIDENT | Créer un membre |
| GET | `/utilisateurs` | ENSEIGNANT, SECRETAIRE, PRESIDENT | Lister tous les membres |
| GET | `/utilisateurs/{id}` | — | Consulter un membre |
| PATCH | `/utilisateurs/{id}` | SECRETAIRE, PRESIDENT | Modifier un membre |
| DELETE | `/utilisateurs/{id}` | PRESIDENT | Supprimer un membre |
| GET | `/utilisateurs/{id}/apte` | — | Vérifier aptitude enseignant (niveau) |
| GET | `/utilisateurs/{id}/niveau` | — | Niveau d'expertise d'un membre |
| GET | `/utilisateurs/{id}/role` | — | Rôle d'un membre |
| GET | `/utilisateurs/{id}/exists` | — | Existence d'un membre |

**Rôles disponibles :** `MEMBRE` · `ENSEIGNANT` · `SECRETAIRE` · `PRESIDENT`

---

### cours-ms — Gestion des cours

Base : **MySQL** · Messaging : **RabbitMQ** (producteur)

| Méthode | Endpoint | Rôle requis | Description |
|---------|----------|-------------|-------------|
| POST | `/cours` | SECRETAIRE, PRESIDENT | Créer un cours |
| GET | `/cours` | — | Lister tous les cours |
| GET | `/cours/{id}` | — | Consulter un cours |
| GET | `/cours/niveau/{niveau}` | — | Cours par niveau (1–5) |
| GET | `/cours/enseignant/{id}` | — | Cours d'un enseignant |
| PATCH | `/cours/{id}` | SECRETAIRE, PRESIDENT | Modifier un cours |
| DELETE | `/cours/{id}` | PRESIDENT | Supprimer un cours |

**Règles métier :** date ≥ J+7 · durée ≥ 45 min · niveau entre 1 et 5 · l'enseignant doit être apte au niveau du cours (vérification via Feign sur utilisateur-ms).

À la création, un événement est publié sur RabbitMQ (exchange `callme`, routing key `cours.created`).

---

### competition-ms — Gestion des compétitions et résultats

Base : **MongoDB**

| Méthode | Endpoint | Rôle requis | Description |
|---------|----------|-------------|-------------|
| POST | `/competitions` | ENSEIGNANT, PRESIDENT | Créer une compétition |
| GET | `/competitions` | — | Lister toutes les compétitions |
| GET | `/competitions/{id}` | — | Consulter une compétition |
| GET | `/competitions/niveau/{niveau}` | — | Compétitions par niveau |
| GET | `/competitions/enseignant/{id}` | — | Compétitions d'un enseignant |
| GET | `/competitions/eleve/{id}` | — | Compétitions accessibles à un élève |
| DELETE | `/competitions/{id}` | PRESIDENT | Supprimer une compétition |
| POST | `/competitions/{id}/resultats` | ENSEIGNANT, PRESIDENT | Ajouter un résultat |
| GET | `/competitions/{id}/resultats` | — | Résultats d'une compétition |
| GET | `/competitions/eleve/{id}/resultats` | — | Résultats d'un élève |
| GET | `/competitions/niveau/{niveau}/count` | — | Nombre de compétitions par niveau |

**Règles métier :** date ≥ J+7 · note entre 0 et 10 (précision 0.5) · l'enseignant doit être apte au niveau · l'élève doit appartenir au bon niveau · un résultat par élève par compétition.

---

### badge-ms — Gestion des badges NFC

Base : **MySQL**

| Méthode | Endpoint | Rôle requis | Description |
|---------|----------|-------------|-------------|
| POST | `/badges` | SECRETAIRE, PRESIDENT | Créer un badge (statut DISPONIBLE) |
| GET | `/badges` | — | Lister tous les badges |
| GET | `/badges/{id}` | — | Consulter un badge |
| PATCH | `/badges/{id}/associer/{idPorteur}` | SECRETAIRE, PRESIDENT | Associer un badge à un membre |
| PATCH | `/badges/{id}/dissocier` | SECRETAIRE, PRESIDENT | Dissocier un badge |
| DELETE | `/badges/{id}` | PRESIDENT | Supprimer un badge |

**Statuts badge :** `DISPONIBLE` → `ASSOCIE` → `DISPONIBLE` (dissociation) · `PERDU` · `DESACTIVE`

---

### presence-ms — Badgeage et présences

Base : **MySQL** · Messaging : **RabbitMQ** (consommateur)

| Méthode | Endpoint | Rôle requis | Description |
|---------|----------|-------------|-------------|
| POST | `/presences/badger` | — | Enregistrer un scan de badge |
| GET | `/presences` | — | Toutes les présences |
| GET | `/presences/eleve/{id}` | — | Présences d'un élève (optionnel: `?debut=&fin=`) |
| GET | `/presences/cours/{id}` | — | Présences à un cours |
| GET | `/presences/cours/{id}/count` | — | Nombre de présences à un cours |
| GET | `/presences/cours/counts` | — | Nombre de présences par cours |

**Règles métier :** un badge ne peut être scanné qu'une fois par cours (409 si doublon) · le badge doit être à l'état ASSOCIE. Le service écoute les événements RabbitMQ de création de cours.

---

### statistiques-ms — Tableaux de bord (Président)

Base : **MySQL** (données pré-agrégées, mises à jour via événements RabbitMQ) · agrège également via **Feign** (presence-ms, cours-ms, competition-ms).

| Méthode | Endpoint | Rôle requis | Description |
|---------|----------|-------------|-------------|
| GET | `/statistiques/cours` | PRESIDENT | Nb de cours + moy. élèves présents |
| GET | `/statistiques/cours/{id}/eleves` | PRESIDENT | Élèves présents à un cours |
| GET | `/statistiques/eleves/{id}/cours` | PRESIDENT | Cours d'un élève avec présences/absences (optionnel: `?debut=&fin=`) |
| GET | `/statistiques/eleves/{id}/competitions` | PRESIDENT | Compétitions d'un élève avec résultats (optionnel: `?debut=&fin=`) |
| GET | `/statistiques/competitions/niveau/{n}/nombre` | PRESIDENT | Nb de compétitions par niveau |

---

## Sécurité

Chaque microservice intègre **Spring Security** avec un filtre personnalisé `RoleHeaderFilter` : le rôle de l'utilisateur connecté est transmis via le header HTTP `X-Role` depuis le frontend. Les endpoints sensibles sont protégés par `@PreAuthorize`.

```
X-Role: PRESIDENT
X-Role: SECRETAIRE
X-Role: ENSEIGNANT
X-Role: MEMBRE
```

---

## Données de test

Les données initiales sont chargées automatiquement au démarrage des conteneurs.

| Login | Mot de passe | Rôle | Niveau |
|-------|-------------|------|--------|
| `presidente` | `password` | PRESIDENT | 5 |
| `secretaire` | `password` | SECRETAIRE | 4 |
| `enseignant3` | `password` | ENSEIGNANT | 3 |
| `enseignant5` | `password` | ENSEIGNANT | 5 |
| `membre1` | `password` | MEMBRE | 1 |
| `membre2` | `password` | MEMBRE | 2 |
| `membre3` | `password` | MEMBRE | 3 |
| `membre4` | `password` | MEMBRE | 4 |
| `membre5` | `password` | MEMBRE | 5 |
| `membre3b` | `password` | MEMBRE | 3 |

---

## Lancement

**Pré-requis :** Docker Desktop · Node.js 18+ (frontend en développement)

### Mode développement (recommandé)

```bash
# 1. Démarrer le backend (bases de données + microservices)
docker compose up --build -d

# 2. Démarrer le frontend avec hot-reload
cd frontend && npm install && npm run dev
# → http://localhost:3000
```

Durée de démarrage backend : **2–3 minutes** (healthchecks MySQL, MongoDB, RabbitMQ).

### Mode production (tout en Docker)

```bash
docker compose --profile prod up --build -d
# Frontend disponible sur http://localhost:3000 via Nginx
```

### Commandes utiles

```bash
docker compose logs -f               # logs en temps réel
docker compose down                  # arrêter (données conservées)
docker compose down -v               # arrêter + supprimer les volumes
docker compose build <service> && docker compose up -d <service>  # rebuild un seul MS
```

---

## Interfaces

| URL | Description |
|-----|-------------|
| http://localhost:3000 | Application web Oduru |
| http://localhost:8080 | API Gateway |
| http://localhost:8761 | Eureka – registre des services |
| http://localhost:8888 | Config Server |
| http://localhost:15672 | RabbitMQ (`guest` / `guest`) |
| http://localhost:9411 | Zipkin – tracing distribué |
| http://localhost:9090 | Prometheus – métriques |

### Swagger par service

| Service | Swagger UI |
|---------|-----------|
| utilisateur-ms | http://localhost:8081/swagger-ui/index.html |
| cours-ms | http://localhost:8082/swagger-ui/index.html |
| competition-ms | http://localhost:8083/swagger-ui/index.html |
| badge-ms | http://localhost:8084/swagger-ui/index.html |
| presence-ms | http://localhost:8085/swagger-ui/index.html |
| statistiques-ms | http://localhost:8086/swagger-ui/index.html |

---

## Tests API

Le fichier `callme-api.http` contient des requêtes HTTP prêtes à l'emploi pour tester l'ensemble des fonctionnalités via le Gateway (port 8080). Compatible IntelliJ IDEA HTTP Client et VS Code REST Client.

---

## Stack technique

| Couche | Technologie |
|--------|-------------|
| Frontend | React 18, Vite, Tailwind CSS, Lucide React |
| Backend | Spring Boot 3, Spring Cloud (Gateway, Eureka, Config Server, OpenFeign) |
| Sécurité | Spring Security 6 (filtre header X-Role) |
| Résilience | Resilience4j (Circuit Breaker, TimeLimiter) |
| Bases de données | MySQL 8 (×4), MongoDB 7 |
| Messaging | RabbitMQ 3 |
| Observabilité | Zipkin, Prometheus |
| Conteneurisation | Docker, Docker Compose |
| Documentation API | SpringDoc OpenAPI (Swagger UI) |