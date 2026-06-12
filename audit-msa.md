# Guide d'audit MSA — Instructions complètes

> À utiliser sur n'importe quel projet MSA Spring Boot  
> Copiez-collez les commandes dans votre terminal depuis la racine du projet

---

## Avant de commencer — règle d'or

**Toujours vérifier le code source, jamais le README seul.**  
Le README décrit ce que l'équipe voulait faire. Le code révèle ce qui a vraiment été fait.  
Chaque affirmation du README doit être confirmée par une commande ci-dessous.

---

## Phase 1 — Structure globale (5 min)

### Ce qu'on cherche
Combien de services, comment sont-ils organisés, est-ce un monorepo ou des dépôts séparés.

### Commandes

```bash
# Voir la structure à la racine
ls -la

# Modules déclarés dans le pom parent
cat pom.xml | grep -A1 "<module>"

# Versions Spring Boot et Spring Cloud utilisées
grep -E "spring-boot.version|spring-cloud.version|spring-boot-starter-parent" pom.xml

# Conteneurs définis dans docker-compose
grep -E "container_name:|image:" docker-compose.yml

# Ports exposés
grep "ports:" -A2 docker-compose.yml
```

### Questions à se poser
- Combien de micro-services y a-t-il ?
- Y a-t-il un pom.xml parent commun (monorepo) ou des pom.xml indépendants ?
- Quels services edge existent (Eureka, Gateway, Config Server) ?
- Les versions Spring Boot et Spring Cloud sont-elles compatibles ?

### Versions compatibles à vérifier

| Spring Boot | Spring Cloud | Java | Statut |
|---|---|---|---|
| 3.4.x | 2024.0.0 | 17 ou 21 | ✅ Recommandé |
| 3.3.x | 2023.0.x | 17 ou 21 | ✅ OK |
| 2.x.x | 2022.x ou moins | 11 ou 17 | ❌ Incompatible avec 3.x |

### Points à noter
- `spring-cloud-starter-netflix-eureka-server` → Eureka présent
- `spring-cloud-starter-gateway` → Gateway présente
- `spring-cloud-config-server` → Config Server présent
- Un seul pom.xml parent = monorepo = couplage de build (signaler)

---

## Phase 2 — Isolation des données (critique)

### Ce qu'on cherche
Chaque service doit avoir sa propre base de données. Pas de jointures entre services.

### Commandes

```bash
# Voir les URLs de datasource de chaque service
grep -r "datasource.url\|DATASOURCE_URL\|SPRING_DATASOURCE" . \
  --include="*.yml" --include="*.yaml" --include="*.properties" \
  | grep -v target | grep -v ".class"

# Même chose dans docker-compose
grep -i "datasource\|mysql\|mongo\|postgres\|redis" docker-compose.yml

# Chercher des jointures JPA cross-service (INTERDIT en MSA)
grep -rn "@ManyToOne\|@OneToMany\|@ManyToMany\|@JoinColumn\|@ForeignKey" \
  . --include="*.java" | grep -v target | grep -v ".class"

# Lister toutes les entités JPA (@Entity) et documents MongoDB (@Document)
grep -rn "@Entity\|@Document" . --include="*.java" \
  | grep -v target | grep -v ".class" | sort

# Chercher les références inter-services (doit être Long ou UUID, pas une entité)
grep -rn "private.*Utilisateur\|private.*Cours\|private.*Badge" \
  . --include="*.java" | grep -v target | grep -v "DTO\|Response\|Request"
```

### Résultats attendus ✅ / problèmes ❌

| Vérification | ✅ Conforme | ❌ Problème |
|---|---|---|
| BDD par service | Chaque service a sa propre URL/conteneur | Plusieurs services partagent la même URL |
| Pas de jointure | Aucun @ManyToOne cross-service | @JoinColumn vers une entité d'un autre service |
| Références par ID | `private Long enseignantId` | `private Utilisateur enseignant` |
| Entités uniques | Chaque entité dans un seul service | Même entité (ex: Presence) dans 2 services |

### Commande de synthèse rapide

```bash
# Nombre de datasource URLs distinctes = doit égaler le nombre de services avec BDD
grep -r "jdbc:\|mongodb://" . --include="*.yml" --include="*.yaml" \
  | grep -v target | sort | uniq
```

---

## Phase 3 — Communication entre services (critique)

### Ce qu'on cherche
Les services doivent communiquer via Feign+Eureka (découverte automatique) et/ou AMQP.  
Les URLs hardcodées violent le principe MSA.

### Commandes

```bash
# Voir tous les @FeignClient — chercher url= (problème) vs name= (correct)
grep -rn "@FeignClient" . --include="*.java" -A3 \
  | grep -v target | grep -E "url=|name=|contextId="

# Si url= est présent, voir les valeurs dans application.yml
grep -rn "services\.\|feign\.\|client\.url" . \
  --include="*.yml" --include="*.yaml" | grep -v target

# Vérifier la présence de Circuit Breaker
grep -rn "resilience4j\|CircuitBreaker\|circuitbreaker\|@CircuitBreaker" \
  . --include="*.xml" --include="*.java" -l | grep -v target

# Configuration Resilience4j
grep -rn "resilience4j" . --include="*.yml" --include="*.yaml" | grep -v target

# Vérifier la présence d'AMQP/RabbitMQ
grep -rn "amqp\|rabbit\|RabbitMQ\|RabbitTemplate\|@RabbitListener\|@EnableRabbit" \
  . --include="*.xml" --include="*.java" -l | grep -v target

# Voir les publishers AMQP (qui publie quoi)
grep -rn "convertAndSend\|rabbitTemplate.send" . --include="*.java" \
  | grep -v target

# Voir les consumers AMQP (qui écoute quoi)
grep -rn "@RabbitListener" . --include="*.java" -A2 | grep -v target

# Vérifier que le load balancer est bien configuré (lb://)
grep -rn "lb://" . --include="*.yml" --include="*.yaml" | grep -v target
```

### Résultats attendus

```
@FeignClient(name = "cours-ms")          ✅ Eureka résout l'adresse
@FeignClient(name = "X", url = "${...}") ❌ URL hardcodée, Eureka ignoré

lb://SERVICE-NAME dans les routes Gateway ✅ Load balancing actif
http://localhost:8082 dans les routes     ❌ Adresse fixe
```

### Points AMQP à vérifier

- Exchange de type `topic` pour la flexibilité des routing keys
- Queues `durable = true` pour survivre aux redémarrages
- `Jackson2JsonMessageConverter` pour la sérialisation JSON automatique
- `@RabbitListener` avec acquittement manuel pour la garantie de livraison

---

## Phase 4 — Sécurité (critique)

### Ce qu'on cherche
Sécurité réelle (JWT/OAuth2) ou simulée (X-Role header). La sécurité simulée est falsifiable.

### Commandes

```bash
# Détecter la sécurité simulée (X-Role header — non sécurisé)
grep -rn "X-Role\|X-UserId\|@RequestHeader.*Role\|RoleCheck" \
  . --include="*.java" | grep -v target | grep -v ".class"

# Détecter OAuth2 / JWT (sécurité réelle)
grep -rn "oauth2\|jwt\|JwtDecoder\|BearerToken\|@PreAuthorize\|@Secured" \
  . --include="*.java" --include="*.yml" -l | grep -v target

# Voir les SecurityConfig existants
find . -name "SecurityConfig.java" | grep -v target
# Et leur contenu
find . -name "SecurityConfig.java" | grep -v target | xargs cat 2>/dev/null

# Vérifier @EnableMethodSecurity (requis pour @PreAuthorize)
grep -rn "@EnableMethodSecurity\|@EnableGlobalMethodSecurity" \
  . --include="*.java" | grep -v target

# Vérifier @PreAuthorize sur les controllers
grep -rn "@PreAuthorize\|@RolesAllowed" . --include="*.java" \
  | grep -v target | head -30

# Vérifier le hashage des mots de passe
grep -rn "passwordEncoder\|BCrypt\|PasswordEncoder\|encode(" \
  . --include="*.java" | grep -v target

# Vérifier que le mot de passe n'est PAS dans les DTOs de réponse
grep -rn "mdp\|password\|motDePasse" . --include="*Response*.java" \
  --include="*DTO*.java" | grep -v target | grep "private"

# Vérifier la configuration issuer-uri (Keycloak)
grep -rn "issuer-uri\|keycloak\|oauth2.resourceserver" \
  . --include="*.yml" --include="*.yaml" | grep -v target
```

### Niveaux de sécurité

```
Niveau 0 (rien)     : aucun contrôle d'accès                    ❌❌
Niveau 1 (simulé)   : X-Role header — falsifiable par curl       ❌
Niveau 2 (partiel)  : Spring Security sans JWT                   ⚠️
Niveau 3 (correct)  : @PreAuthorize + JWT Keycloak               ✅
```

### FeignJwtInterceptor — à vérifier si OAuth2 est implémenté

```bash
# Vérifier que le JWT est propagé dans les appels Feign inter-services
find . -name "FeignJwt*.java" -o -name "*FeignInterceptor*.java" | grep -v target
grep -rn "RequestInterceptor\|RequestTemplate\|Authorization.*Bearer" \
  . --include="*.java" | grep -v target
```

---

## Phase 5 — Exposition de l'API (important)

### Ce qu'on cherche
Les controllers ne doivent jamais retourner des entités JPA directement.  
Les entrées (POST/PUT) doivent utiliser des DTOs de requête avec validation.

### Commandes

```bash
# Lister tous les DTOs de réponse existants
find . -name "*Response*.java" -o -name "*DTO*.java" -o -name "*Dto*.java" \
  | grep -v target | sort

# Lister tous les DTOs de requête existants
find . -name "*Request*.java" -o -name "*Form*.java" \
  | grep -v target | sort

# Détecter les entités retournées directement (PROBLÈME)
# Un controller qui retourne Cours, Utilisateur, Badge, etc. directement
grep -rn "public.*Cours\b\|public.*Utilisateur\b\|public.*Badge\b\|public.*Presence\b" \
  . --include="*Controller*.java" | grep -v target | grep -v "//\|import"

# Détecter les entités acceptées en body (PROBLÈME)
grep -rn "@RequestBody Cours\|@RequestBody Utilisateur\|@RequestBody Badge" \
  . --include="*.java" | grep -v target

# Vérifier les validations @Valid sur les requêtes
grep -rn "@Valid\|@Validated" . --include="*.java" \
  | grep -v target | grep -v "//\|import"

# Vérifier @NotNull @NotBlank @Min @Max dans les DTOs de requête
grep -rn "@NotNull\|@NotBlank\|@Email\|@Min\|@Max\|@Size" \
  . --include="*Request*.java" | grep -v target

# Vérifier le GlobalExceptionHandler
grep -rn "@RestControllerAdvice\|@ExceptionHandler" \
  . --include="*.java" -l | grep -v target

# Vérifier les codes HTTP retournés (201 pour POST, 204 pour DELETE)
grep -rn "ResponseEntity.status\|HttpStatus.CREATED\|HttpStatus.NO_CONTENT" \
  . --include="*Controller*.java" | grep -v target | head -20
```

### Signaux d'alerte

```bash
# Si cette commande retourne des résultats → problème DTO
grep -rn "return.*repository\.\|return.*repo\." \
  . --include="*Controller*.java" | grep -v target
```

---

## Phase 6 — Infrastructure edge (important)

### Ce qu'on cherche
Les 4 composants MSA de la couche infrastructure.

### Commandes

```bash
# 1. EUREKA — annuaire de services
grep -rn "@EnableEurekaServer" . --include="*.java" | grep -v target
grep -rn "@EnableDiscoveryClient" . --include="*.java" -l | grep -v target
# Vérifier que les services s'enregistrent dans Eureka
grep -rn "eureka.client.serviceUrl" . --include="*.yml" | grep -v target

# 2. API GATEWAY
find . -iname "*gateway*" -type d | grep -v target
find . -iname "*gateway*" -name "pom.xml" | grep -v target
grep -rn "spring-cloud-starter-gateway" . --include="*.xml" | grep -v target
# Routes configurées
grep -rn "predicates\|Path=\|RewritePath" . --include="*.yml" | grep -v target

# 3. CONFIG SERVER
grep -rn "spring-cloud-config-server\|@EnableConfigServer" \
  . --include="*.xml" --include="*.java" | grep -v target
# Les services pointent-ils vers le Config Server ?
grep -rn "spring.config.import\|configserver:" \
  . --include="*.yml" --include="*.yaml" | grep -v target

# 4. LOAD BALANCER
grep -rn "spring-cloud-starter-loadbalancer" . --include="*.xml" | grep -v target
grep -rn "@LoadBalancerClients" . --include="*.java" | grep -v target
```

### Synthèse edge services

```bash
# Résumé rapide de ce qui existe
echo "=== Eureka ===" && grep -rl "EnableEurekaServer" . | grep -v target | head -3
echo "=== Gateway ===" && find . -name "*.java" | xargs grep -l "spring-cloud-starter-gateway" 2>/dev/null | grep -v target | head -3
echo "=== Config ===" && grep -rl "EnableConfigServer" . | grep -v target | head -3
echo "=== RabbitMQ ===" && grep -rl "RabbitTemplate\|@RabbitListener" . | grep -v target | head -5
echo "=== Resilience4j ===" && grep -rl "resilience4j" . --include="*.xml" | grep -v target | head -5
```

---

## Phase 7 — Observabilité (bonus)

### Commandes

```bash
# Actuator — exposé sur quels endpoints ?
grep -rn "actuator\|management.endpoints" . --include="*.yml" | grep -v target

# Zipkin — tracing distribué
grep -rn "zipkin\|micrometer-tracing\|zipkin-reporter" \
  . --include="*.yml" --include="*.xml" | grep -v target

# Prometheus — métriques
grep -rn "prometheus\|micrometer-registry-prometheus" \
  . --include="*.yml" --include="*.xml" | grep -v target

# SpringDoc Swagger UI
grep -rn "springdoc\|swagger-ui" . --include="*.xml" --include="*.yml" \
  | grep -v target | head -10

# Vérifier que Spring Sleuth N'EST PAS utilisé (incompatible Spring Boot 3+)
grep -rn "spring-cloud-starter-sleuth\|sleuth" . --include="*.xml" | grep -v target
```

### Points d'attention

- `micrometer-tracing-bridge-brave` + `zipkin-reporter-brave` → correct pour Spring Boot 3
- `spring-cloud-starter-sleuth` → **incompatible avec Spring Boot 3**, à signaler
- `management.tracing.sampling.probability: 1.0` → 100% des traces collectées (dev OK)

---

## Phase 8 — Qualité du code (important)

### Commandes

```bash
# lib-commun — contenu
find . -path "*/lib-commun/src/main/java*" -name "*.java" | grep -v target | sort

# lib-commun ne doit PAS avoir @SpringBootApplication
grep -rn "@SpringBootApplication" . --include="*.java" \
  | grep -i "commun\|lib\|shared" | grep -v target

# lib-commun ne doit PAS contenir de logique métier (Role, enum...)
grep -rn "enum Role\|class Role\|class RoleCheck" \
  . --include="*.java" | grep -v target

# Tests — y en a-t-il ?
find . -name "*Test*.java" | grep -v target | sort
find . -name "*Test*.java" | grep -v target | xargs wc -l 2>/dev/null | sort -rn | head -10

# Tests vides (classe de test sans méthode de test)
grep -rn "@Test" . --include="*Test*.java" | grep -v target | wc -l

# Contraintes sur les entités
grep -rn "@Column(nullable\|@NotNull\|unique = true" \
  . --include="*.java" -l | grep -v target | grep -i entity

# Lombok utilisé (économise du code)
grep -rn "@Data\|@Builder\|@NoArgsConstructor\|@AllArgsConstructor" \
  . --include="*.java" -l | grep -v target | wc -l
```

---

## Phase 9 — Cohérence MSA (critique)

### Commandes

```bash
# Vérifier que le projet compile sans erreur
mvn clean package -DskipTests 2>&1 | tail -15
# Doit se terminer par : BUILD SUCCESS

# Lancer et vérifier que tout démarre
docker compose up -d 2>&1 | tail -20
sleep 30
docker compose ps
# Tous les conteneurs doivent être "running" ou "healthy"

# Vérifier Eureka (tous les services enregistrés)
curl -s http://localhost:8761/eureka/apps | grep -o "<app>.*</app>" | head -20
# Ou simplement ouvrir http://localhost:8761 dans le navigateur

# Vérifier la Gateway (route de base)
curl -s http://localhost:8080/actuator/health | python3 -m json.tool 2>/dev/null || \
  curl -s http://localhost:8080/actuator/health

# Test end-to-end rapide — créer un utilisateur et vérifier la réponse
curl -s -X POST http://localhost:8080/api/utilisateurs \
  -H "Content-Type: application/json" \
  -d '{"nom":"Test","prenom":"User","email":"test@test.fr","login":"testuser","mdp":"secret123","niveauExpertise":1,"role":"MEMBRE","adresse":{"ville":"Paris","pays":"France"}}' \
  | python3 -m json.tool

# Vérifier RabbitMQ (si implémenté)
curl -s -u guest:guest http://localhost:15672/api/queues | \
  python3 -m json.tool 2>/dev/null | grep "name\|messages"
```

### Vérification README vs réalité

```bash
# Pour chaque affirmation du README, vérifier dans le code :

# "API Gateway"
find . -name "*Gateway*Application*.java" | grep -v target

# "RabbitMQ / AMQP"
grep -rl "RabbitTemplate\|@RabbitListener" . --include="*.java" | grep -v target

# "Resilience4j / Circuit Breaker"
grep -rl "resilience4j" . --include="*.xml" | grep -v target

# "MySQL isolé par service"
grep -o "mysql-[a-z]*" docker-compose.yml | sort | uniq

# "DTO pattern"
find . -name "*Response*.java" | grep -v target | wc -l

# "Zipkin tracing"
grep -rl "zipkin" . --include="*.yml" | grep -v target
```

---

## Phase 10 — Frontend (si applicable)

### Commandes

```bash
# Vérifier l'existence d'un frontend
ls -la frontend/ 2>/dev/null || echo "Pas de frontend"
ls -la front/ 2>/dev/null
find . -name "package.json" | grep -v node_modules | grep -v target

# Framework utilisé
cat frontend/package.json 2>/dev/null | grep -E '"react"\|"angular"\|"vue"'

# Le frontend passe-t-il par la Gateway ?
grep -E "proxy|localhost:8080|8080" frontend/vite.config.* 2>/dev/null
grep "proxy_pass" frontend/nginx.conf 2>/dev/null

# Le frontend est-il dockerisé ?
cat frontend/Dockerfile 2>/dev/null | head -20
grep "frontend\|front" docker-compose.yml | head -10

# Les droits sont-ils gérés côté UI (rôles, affichage conditionnel)
grep -rn "role\|Role\|PRESIDENT\|SECRETAIRE\|ENSEIGNANT" \
  frontend/src --include="*.jsx" --include="*.tsx" --include="*.vue" 2>/dev/null \
  | grep -v "node_modules" | head -20
```

---

## Rapport d'audit — template à remplir

```
PROJET : _______________
DATE   : _______________
ÉQUIPE : _______________

PHASE 1 — Structure
[ ] Nombre de services : ___
[ ] Monorepo ou dépôts séparés : ___
[ ] Spring Boot version : ___  Spring Cloud : ___
[ ] Services edge présents : Eureka ___ Gateway ___ Config ___

PHASE 2 — Isolation des données
[ ] BDD isolée par service : OUI / NON
[ ] Nombre de conteneurs BDD : ___
[ ] @ManyToOne cross-service : OUI / NON
[ ] Entités dupliquées : OUI / NON — lesquelles : ___

PHASE 3 — Communication
[ ] Feign avec Eureka (name=) : OUI / NON
[ ] URLs hardcodées (url=) : OUI / NON
[ ] Circuit Breaker Resilience4j : OUI / NON
[ ] AMQP RabbitMQ : OUI / NON — événements : ___

PHASE 4 — Sécurité
[ ] Niveau : simulée (X-Role) / @PreAuthorize / JWT+Keycloak
[ ] SecurityConfig présent : OUI / NON
[ ] Mot de passe hashé et absent des réponses : OUI / NON
[ ] FeignJwtInterceptor (si OAuth2) : OUI / NON

PHASE 5 — API
[ ] DTOs en réponse (pas d'entités JPA) : OUI / NON
[ ] DTOs en requête avec @Valid : OUI / NON
[ ] GlobalExceptionHandler : OUI / NON
[ ] Codes HTTP corrects (201, 204...) : OUI / NON

PHASE 6 — Edge services
[ ] Eureka : OUI / NON
[ ] Gateway : OUI / NON
[ ] Config Server : OUI / NON
[ ] Load Balancer (lb://) : OUI / NON

PHASE 7 — Observabilité
[ ] Actuator : OUI / NON
[ ] Zipkin : OUI / NON
[ ] Prometheus : OUI / NON
[ ] Swagger : OUI / NON

PHASE 8 — Qualité
[ ] lib-commun sans @SpringBootApplication : OUI / NON
[ ] lib-commun sans logique métier : OUI / NON
[ ] Tests présents : OUI / NON — nombre : ___
[ ] Contraintes BDD (@NotNull, unique) : OUI / NON

PHASE 9 — Cohérence
[ ] mvn clean package → BUILD SUCCESS : OUI / NON
[ ] docker compose up → tous UP : OUI / NON
[ ] README conforme au code : OUI / NON

PHASE 10 — Frontend
[ ] Frontend présent : OUI / NON
[ ] Passe par Gateway : OUI / NON
[ ] Droits respectés côté UI : OUI / NON
[ ] Dockerisé : OUI / NON

SCORE ESTIMÉ : ___ / 100

PROBLÈMES CRITIQUES (priorité haute) :
1. ___
2. ___
3. ___

PROBLÈMES IMPORTANTS (priorité moyenne) :
1. ___
2. ___

MANQUES (à implémenter) :
1. ___
2. ___
```

---

## Commande d'audit express (5 min chrono)

Copier-coller ce bloc entier dans le terminal depuis la racine du projet :

```bash
echo "===== AUDIT MSA EXPRESS =====" && \
echo "" && \
echo "--- Structure ---" && \
ls && \
echo "" && \
echo "--- Spring versions ---" && \
grep -E "spring-boot-starter-parent|spring-cloud.version" pom.xml 2>/dev/null | head -4 && \
echo "" && \
echo "--- BDD par service ---" && \
grep -r "jdbc:\|mongodb://" . --include="*.yml" --include="*.yaml" | grep -v target | grep -v "#" && \
echo "" && \
echo "--- Feign (name= vs url=) ---" && \
grep -r "@FeignClient" . --include="*.java" -A2 | grep -E "name=|url=" | grep -v target && \
echo "" && \
echo "--- Sécurité (X-Role = simulé) ---" && \
grep -r "X-Role\|X-UserId" . --include="*.java" | grep -v target | wc -l | xargs echo "Occurrences X-Role header :" && \
grep -r "@PreAuthorize" . --include="*.java" | grep -v target | wc -l | xargs echo "Occurrences @PreAuthorize :" && \
echo "" && \
echo "--- AMQP ---" && \
grep -r "RabbitTemplate\|@RabbitListener" . --include="*.java" | grep -v target | wc -l | xargs echo "Occurrences AMQP :" && \
echo "" && \
echo "--- DTOs ---" && \
find . -name "*Response*.java" -o -name "*DTO*.java" | grep -v target | wc -l | xargs echo "DTOs trouvés :" && \
echo "" && \
echo "--- Edge services ---" && \
grep -r "EnableEurekaServer\|EnableConfigServer" . --include="*.java" | grep -v target && \
find . -name "*Gateway*.java" | grep -v target && \
echo "" && \
echo "===== FIN AUDIT EXPRESS ====="
```

---

*Guide d'audit MSA — M2 MIAGE ITN — Université de Toulouse*

---

---

# Rapport d'audit — Oduru / CallMe

```
PROJET : Oduru (CallMe) — Gestion d'un club de danse rythmique
DATE   : 2026-06-11
ÉQUIPE : Ha Thu Dinh & Katia Bouarab
```

---

## PHASE 1 — Structure

- [x] Nombre de services : **10** (lib-commun, eureka-server, config-server, gateway + 6 microservices métier)
- [x] Monorepo ou dépôts séparés : **Monorepo** (pom.xml parent commun → couplage de build, signaler)
- [x] Spring Boot version : **3.4.3**  Spring Cloud : **2024.0.0**  Java : **21** ✅ Versions compatibles
- [x] Services edge présents : Eureka ✅  Gateway ✅  Config Server ✅
- [x] Conteneurs Docker : **18** (4 MySQL, MongoDB, RabbitMQ, Eureka, Zipkin, Prometheus, Config Server, Gateway, 6 microservices, Frontend)

---

## PHASE 2 — Isolation des données

- [x] BDD isolée par service : **OUI** ✅
- [x] Nombre de conteneurs BDD : **5** (mysql-utilisateur:3307 · mysql-cours:3308 · mysql-badge:3309 · mysql-presence:3310 · mongodb:27017)
- [x] @ManyToOne cross-service : **NON** ✅ (aucune jointure JPA inter-services)
- [x] Entités dupliquées : **NON** ✅ (6 entités distinctes, chacune dans son seul microservice)
- [x] Références par ID (Long) : **OUI** ✅ (enseignantId, eleveId, idBadge — jamais d'objet JPA cross-service)

> ✅ statistiques-ms n'a intentionnellement aucune BDD propre — il est un agrégateur pur via Feign.

---

## PHASE 3 — Communication

- [x] Feign avec Eureka (name=) : **OUI** ✅ — tous les @FeignClient utilisent `name = "xxx-ms"`, jamais `url=`
- [x] URLs hardcodées (url=) : **NON** ✅
- [x] Circuit Breaker Resilience4j : **OUI** ✅ — configuré dans cours-ms, competition-ms, badge-ms, presence-ms, statistiques-ms
- [x] Load Balancer lb:// : **OUI** ✅ — toutes les routes Gateway utilisent `lb://SERVICE-NAME`
- [x] AMQP RabbitMQ : **OUI** ✅
  - cours-ms → `rabbitTemplate.convertAndSend()` (exchange `callme`, routing key `cours.created`)
  - presence-ms → `@RabbitListener(queues = QUEUE_COURS)` (consommateur)

> ℹ Feign inter-services : badge-ms, cours-ms, competition-ms → utilisateur-ms · presence-ms → badge-ms + cours-ms + utilisateur-ms · statistiques-ms → tous les autres

---

## PHASE 4 — Sécurité

- [x] Niveau : **simulée (X-Role header)** ⚠️ — falsifiable par `curl -H "X-Role: PRESIDENT"`
- [x] SecurityConfig présent : **OUI** ✅ — dans les 6 microservices + @EnableMethodSecurity
- [x] @PreAuthorize : **OUI** ✅ — 18 occurrences sur les endpoints sensibles
- [x] FeignJwtInterceptor (OAuth2) : **N/A** — sécurité par header, pas de JWT

### ❌❌ PROBLÈME CRITIQUE — Mot de passe stocké et comparé en clair

```java
// utilisateur-ms/service/UtilisateurService.java
if (!u.getIdConnexion().getMdp().equals(mdp)) {   // ← comparaison en clair !
    throw new ApiException(HttpStatus.UNAUTHORIZED, "Identifiants incorrects");
}
```

Le mot de passe est stocké en base sans hashage et comparé directement par `equals()`.  
**BCryptPasswordEncoder n'est pas utilisé.** C'est une faille de sécurité majeure.

**Correction nécessaire :**
```java
// Dans SecurityConfig ou @Bean séparé :
@Bean
public BCryptPasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

// À la création du membre :
u.getIdConnexion().setMdp(passwordEncoder.encode(request.getMdp()));

// Au login :
if (!passwordEncoder.matches(mdp, u.getIdConnexion().getMdp())) { ... }
```

- [x] Mot de passe absent des DTOs réponse : **OUI** ✅ — UtilisateurResponse ne contient pas le champ mdp

---

## PHASE 5 — Exposition de l'API

- [x] DTOs en réponse (pas d'entités JPA) : **OUI** ✅ — 8 classes *Response, aucun retour d'entité JPA direct
- [x] DTOs en requête avec @Valid : **OUI** ✅ — 9 classes *Request, 11 occurrences @Valid, 30 contraintes (@NotNull/@NotBlank/@Min/@Max)
- [x] GlobalExceptionHandler : **OUI** ✅ — centralisé dans lib-commun (ApiException, MethodArgumentNotValidException, FeignException, NoFallbackAvailableException)
- [x] Retour direct repository dans controller : **NON** ✅ — aucun `return repo.xxx()` dans les controllers

### ⚠️ Codes HTTP incomplets

- DELETE → `204 No Content` ✅ (ResponseEntity.noContent())
- POST → pas de `201 Created` détecté ⚠️ — les créations retournent vraisemblablement `200 OK` au lieu de `201 Created`

```java
// Exemple de correction pour les endpoints POST :
return ResponseEntity.status(HttpStatus.CREATED).body(service.creer(request));
```

---

## PHASE 6 — Edge services

- [x] Eureka : **OUI** ✅ — port 8761, @EnableEurekaServer
- [x] Gateway : **OUI** ✅ — Spring Cloud Gateway, port 8080
- [x] Config Server : **OUI** ✅ — Spring Cloud Config, port 8888, backend native
- [x] Load Balancer (lb://) : **OUI** ✅ — toutes les 6 routes Gateway
- [x] Import Config Server dans les microservices : **OUI** ✅ — `optional:configserver:` dans les 7 services (fallback local garanti)

---

## PHASE 7 — Observabilité

- [x] Actuator : **OUI** ✅ — health, info, prometheus, metrics exposés sur tous les services
- [x] Zipkin : **OUI** ✅ — micrometer-tracing-bridge-brave + zipkin-reporter-brave (Spring Boot 3 compatible), 100% sampling
- [x] Prometheus : **OUI** ✅ — micrometer-registry-prometheus, prometheus.yml configuré
- [x] Swagger : **OUI** ✅ — springdoc-openapi (36 déclarations) sur les 6 microservices

> ✅ Pas de spring-cloud-starter-sleuth (incompatible Spring Boot 3) — bonne pratique respectée.

---

## PHASE 8 — Qualité du code

- [x] lib-commun sans @SpringBootApplication : **OUI** ✅
- [x] lib-commun contenu : `ApiException.java` + `GlobalExceptionHandler.java` — **MAIS**

### ⚠️ Duplication non nécessaire dans lib-commun

`enum Role` et `RoleHeaderFilter` sont **dupliqués dans les 6 microservices** au lieu d'être mutualisés dans lib-commun.  
Ce sont des composants techniques transversaux (pas de logique métier spécifique), leur place naturelle est lib-commun.

- [x] Tests présents : **OUI** ✅
  - `CoursServiceTests` : **14 tests** (règles métier, validation date, durée, niveau, aptitude enseignant)
  - `UtilisateurServiceTest` : **9 tests** (CRUD, login, cas d'erreur)
  - 4 classes `ApplicationTests` (contextLoads uniquement)
  - **Total : 23 tests unitaires réels**
  
- [x] `<parameters>true</parameters>` : **OUI** ✅ — dans les 7 pom.xml microservices
- [x] Contraintes de validation : **OUI** ✅ — 30 annotations Bean Validation dans les DTOs Request

---

## PHASE 9 — Cohérence MSA

- [ ] mvn clean package → BUILD SUCCESS : **à vérifier** (non exécuté dans cet audit)
- [ ] docker compose up → tous UP : **à vérifier** (non exécuté dans cet audit)
- [x] README conforme au code : **OUI** ✅ — chaque affirmation du README a été vérifiée et confirmée dans le code

---

## PHASE 10 — Frontend

- [x] Frontend présent : **OUI** ✅ — React 18 + Vite + Tailwind CSS
- [x] Passe par Gateway : **OUI** ✅
  - Dev : `vite.config.js` proxy `/api` → `http://localhost:8080`
  - Prod : `nginx.conf` `proxy_pass http://callme-gateway:8080/api/`
- [x] Droits respectés côté UI : **OUI** ✅ — `role` propagé à toutes les vues, boutons masqués selon rôle, double vérification backend via @PreAuthorize
- [x] Dockerisé : **OUI** ✅ — Dockerfile multi-stage (build Vite → Nginx), port 3000

---

## SCORE ESTIMÉ : 78 / 100

| Phase | Points | Obtenu | Détail |
|-------|--------|--------|--------|
| Structure | 10 | 9 | Monorepo (couplage build) |
| Isolation données | 15 | 15 | Parfait — 5 BDD séparées |
| Communication | 15 | 15 | Feign Eureka + CB + AMQP |
| Sécurité | 15 | 6 | X-Role simulé + **mot de passe en clair** |
| Exposition API | 10 | 8 | Pas de 201 Created sur les POST |
| Edge services | 10 | 10 | Eureka + Gateway + Config Server |
| Observabilité | 10 | 10 | Zipkin + Prometheus + Swagger |
| Qualité | 10 | 7 | Role/RoleHeaderFilter dupliqués |
| Frontend | 5 | 5 | Gateway + droits + Docker |

---

## PROBLÈMES CRITIQUES (priorité haute)

1. **Mot de passe stocké et comparé en CLAIR** (`equals(mdp)` dans UtilisateurService)  
   → Implémenter BCryptPasswordEncoder à la création et au login

2. **Sécurité simulée via X-Role header** — falsifiable sans authentification  
   → Acceptable pour un projet pédagogique mais à documenter explicitement comme limitation  
   → En production : remplacer par JWT / Keycloak

---

## PROBLÈMES IMPORTANTS (priorité moyenne)

1. **`enum Role` et `RoleHeaderFilter` dupliqués dans les 6 microservices**  
   → Les déplacer dans lib-commun pour éviter la dérive de cohérence

2. **POST créations retournent `200 OK` au lieu de `201 Created`**  
   → `ResponseEntity.status(HttpStatus.CREATED).body(...)` dans tous les endpoints POST

---

## POINTS FORTS

1. Isolation des données irréprochable (5 BDD séparées, aucune jointure cross-service)
2. Communication Feign avec découverte Eureka — aucune URL hardcodée
3. Circuit Breaker Resilience4j sur tous les services consommateurs
4. Messaging asynchrone RabbitMQ correctement implémenté
5. Config Server Spring Cloud avec fallback `optional:`
6. Observabilité complète (Zipkin, Prometheus, Swagger)
7. 23 tests unitaires réels avec Mockito (pas juste des contextLoads)
8. DTOs Request/Response complets avec validation Bean
9. GlobalExceptionHandler centralisé dans lib-commun
10. Frontend dockerisé passant intégralement par la Gateway