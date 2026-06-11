# Projet CallMe/Oduru

### Binôme : Ha Thu & Katia 
### Module Architectures Microservices sur Cloud
### Master MIAGE M2 - ITN

---

## Architecture

```
Frontend (React)
      │
      ▼
  Gateway :8080  ◄──── Eureka :8761
      │
      ├── utilisateur-ms :8081  ── MySQL :3307
      ├── cours-ms       :8082  ── MySQL :3308  ──► RabbitMQ ──► presence-ms
      ├── badge-ms       :8084  ── MySQL :3309
      ├── presence-ms    :8085  ── MySQL :3310
      ├── competition-ms :8083  ── MongoDB :27017
      └── statistiques-ms:8086  (agrège via Feign)
```

**Outils MSA :** Eureka · API Gateway + Circuit Breaker (Resilience4j) · Messaging async (RabbitMQ) · Tracing (Zipkin) · Métriques (Prometheus)

---

## Lancement

**Pré-requis :** Java 21 · Maven 3.9+ · Docker Desktop

```bash
# 1. Compiler
mvn clean package -DskipTests

# 2. Démarrer tout
docker compose up --build -d

# 3. Vérifier
docker compose ps
```

Démarrage complet : ~2-3 min (attente healthchecks MySQL/MongoDB/RabbitMQ).

### Interfaces disponibles

| URL | Description |
|-----|-------------|
| http://localhost:3000 | Frontend React |
| http://localhost:8080 | API Gateway |
| http://localhost:8761 | Eureka Dashboard |
| http://localhost:15672 | RabbitMQ (`guest/guest`) |
| http://localhost:9411 | Zipkin |
| http://localhost:9090 | Prometheus |

### Swagger par service

| Service | Swagger UI |
|---------|-----------|
| utilisateur-ms | http://localhost:8081/swagger-ui/index.html |
| cours-ms | http://localhost:8082/swagger-ui/index.html |
| competition-ms | http://localhost:8083/swagger-ui/index.html |
| badge-ms | http://localhost:8084/swagger-ui/index.html |
| presence-ms | http://localhost:8085/swagger-ui/index.html |
| statistiques-ms | http://localhost:8086/swagger-ui/index.html |

```bash
# Arrêter (sans supprimer les données)
docker compose down

# Arrêter + supprimer les volumes
docker compose down -v
```

---

## Authentification
Le rôle est transmis via le header `X-Role` sur les endpoints protégés.

Valeurs : `MEMBRE` · `ENSEIGNANT` · `SECRETAIRE` · `PRESIDENT`

---

## Tests

Le fichier `callme-api.http` contient des requêtes HTTP pré-écrites pour tester les fonctionnalités principales de l'API via le Gateway (port 8080).