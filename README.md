# Oduru / CallMe

Gestion d'un club de danse rythmique : 6 microservices Spring Boot, React frontend.

**Binome :** Ha Thu Dinh & Katia Bouarab ; Master MIAGE M2 ITN

---

## Lancement

```bash
# Copier les variables d'environnement
cp .env.example .env   # ou utiliser le .env fourni

# Demarrer tout le backend
docker compose up --build -d

# Frontend en dev (hot-reload)
cd frontend && npm install && npm run dev
```

Attendre 2-3 minutes le temps des healthchecks.

---

## Interfaces

| URL | Description |
|-----|-------------|
| http://localhost:3000 | Application web |
| http://localhost:8080 | API Gateway |
| http://localhost:8761 | Eureka |
| http://localhost:15672 | RabbitMQ (voir .env pour credentials) |
| http://localhost:9411 | Zipkin |
| http://localhost:9090 | Prometheus |
| http://localhost:3001 | Grafana |

Swagger par service : `http://localhost:808{1-6}/swagger-ui/index.html`

---

## Comptes de test (mot de passe : `password`)

| Login | Role | Niveau |
|-------|------|--------|
| `presidente` | PRESIDENT | 5 |
| `secretaire` | SECRETAIRE | 4 |
| `enseignant3` | ENSEIGNANT | 3 |
| `enseignant5` | ENSEIGNANT | 5 |
| `membre1` a `membre5` | MEMBRE | 1 a 5 |
| `membre3b` | MEMBRE | 3 |

---

## Architecture

```
Frontend React/Vite (Nginx)      :3000
          |
API Gateway (Spring Cloud)       :8080
          |
  +-------+-------+-------+-------+-------+
  |       |       |       |       |       |
util    cours  compet  badge  presence stats
:8081  :8082  :8083  :8084  :8085  :8086
MySQL  MySQL  Mongo  MySQL  MySQL  MySQL

Eureka :8761  Config Server :8888
RabbitMQ :5672/:15672  Zipkin :9411  Prometheus :9090
```

**Communication :**
- Feign + Eureka : cours-ms et competition-ms vers utilisateur-ms, presence-ms vers badge-ms + cours-ms + utilisateur-ms
- RabbitMQ (exchange `callme.events`) : utilisateur-ms, cours-ms, competition-ms, presence-ms publient ; statistiques-ms et badge-ms consomment

---

## Commandes utiles

```bash
docker compose logs -f <service>
docker compose build <service> && docker compose up -d <service>
docker compose down          # arret (donnees conservees)
docker compose down -v       # arret + suppression volumes
```

---

## Stack

Spring Boot 3.4 / Spring Cloud 2024 / Java 21 / React 18 / Vite / Tailwind CSS / MySQL 8 / MongoDB 7 / RabbitMQ 3 / Docker Compose
