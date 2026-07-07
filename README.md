# Banking Service — ABC Bank

A microservices-based banking application built with **Spring Boot 3 / Java 17**, using **JWT-based role authentication & authorization**, **H2** in-memory database, Docker, and Kubernetes with **Horizontal Pod Autoscaling**.

- Base package: `com.sangam.abcbank`
- Project name: **Banking service**

## Architecture

Two independent microservices, each with its own H2 database and deployable independently:

| Service           | Port | Responsibility                                                          |
|-------------------|------|-------------------------------------------------------------------------|
| `user-service`    | 8081 | User registration/login, role management, JWT issuance                  |
| `banking-service` | 8082 | Account creation/update, cash deposit, cash withdrawal, balance enquiry |
| `Loan-service`    | 8083 | apply loan, approve loan, cash withdrawal, disburse Loan,deleteLoan     |


Authentication flow:

1. `user-service` authenticates credentials and issues a signed JWT containing the username and roles (`ROLE_ADMIN` / `ROLE_USER`).
2. The client sends that JWT as a `Bearer` token to `banking-service`.
3. `banking-service` independently validates the JWT signature using the **same shared secret** (`jwt.secret`) — no network call back to `user-service` is required (stateless JWT validation), which keeps the services independently scalable.
4. `loan-service` independently validates the JWT signature using the **same shared secret** (`jwt.secret`) — no network call back to `user-service` is required (stateless JWT validation), which keeps the services independently scalable.


```
Client -> POST /api/auth/login (user-service) -> JWT
Client -> Authorization: Bearer <JWT> -> banking-service APIs
```

## Roles

- `ADMIN` — manage users & roles, view all accounts across the bank.
- `CUSTOMER` — create/manage their own accounts, deposit, withdraw, check balance, apply loans.
- `LOAN_OFFICER` — reviews applied loans applied by customer.
- `MANAGER` — disburse Loan applied by customer.


A default admin is seeded on first startup of `user-service`:
```
username: admin
password: Admin@123
```
**Change or remove this in `DataSeeder.java` before any real deployment.**

## API Reference

### user-service (`localhost:8081`)


### banking-service (`localhost:8082`)


### loan-service (`localhost:8083`)



## Running Locally (Maven)

```bash
# Build everything
mvn clean package

# Run user-service
cd user-service && mvn spring-boot:run

# Run banking-service (in another terminal)
cd banking-service && mvn spring-boot:run

# Run loan-service (in another terminal)
cd loan-service && mvn spring-boot:run
```

H2 consoles:
- user-service: http://localhost:8081/h2-console (JDBC URL: `jdbc:h2:mem:userdb`, user `sa`, pass `password`)
- banking-service: http://localhost:8082/h2-console (JDBC URL: `jdbc:h2:mem:bankingdb`, user `sa`, pass `password`)
- loan-service: http://localhost:8083/h2-console (JDBC URL: `jdbc:h2:mem:loandb`, user `sa`, pass `password`)

## Running with Docker Compose

```bash
docker compose up --build
```

## Deploying to Kubernetes (with Autoscaling)

Manifests live under `k8s/` (shared namespace + secret) and `<service>/k8s/` (per-service Deployment, Service, HorizontalPodAutoscaler).

```bash
# 1. Build & push images (adjust registry/tag as needed)
docker build -f user-service/Dockerfile -t abcbank/user-service:1.0.0 .
docker build -f banking-service/Dockerfile -t abcbank/banking-service:1.0.0 .
docker build -f loan-service/Dockerfile -t abcbank/loan-service:1.0.0 .

# 2. Install the metrics-server add-on (required for HPA CPU/memory metrics)
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml

# 3. Create namespace + shared JWT secret
kubectl apply -f k8s/namespace.yaml

# 4. Deploy user-service
kubectl apply -f user-service/k8s/deployment.yaml
kubectl apply -f user-service/k8s/service.yaml
kubectl apply -f user-service/k8s/hpa.yaml

# 5. Deploy banking-service
kubectl apply -f banking-service/k8s/deployment.yaml
kubectl apply -f banking-service/k8s/service.yaml
kubectl apply -f banking-service/k8s/hpa.yaml

# 6. Deploy loan-service
kubectl apply -f loan-service/k8s/deployment.yaml
kubectl apply -f loan-service/k8s/service.yaml
kubectl apply -f loan-service/k8s/hpa.yaml

# 7. (Optional) expose both via a single ingress
kubectl apply -f k8s/ingress.yaml

# Check autoscaler status
kubectl get hpa -n abcbank
```

### How autoscaling works here

- Each Deployment defines CPU/memory `requests` and `limits` (mandatory for HPA to calculate utilization).
- Each `HorizontalPodAutoscaler` (autoscaling/v2) targets CPU utilization (65% for user-service, 60% for banking-service) and memory utilization (75%), scaling between `minReplicas: 2` and `maxReplicas` (10 for user-service, 15 for banking-service since it's typically higher traffic).
- `behavior` blocks tune how aggressively pods scale up (fast, 100%/30s) vs. scale down (conservative, 25%/60s with a 2-minute stabilization window) to avoid flapping.
- The `metrics-server` add-on must be running in the cluster for the HPA to receive resource metrics.

## Security Notes

- Passwords are hashed with BCrypt.
- JWTs are signed with HMAC-SHA256 using a shared secret (`jwt.secret`), configurable via the `JWT_SECRET` environment variable — **must be identical across both services**.
- In Kubernetes, the secret is stored in a `Secret` object (`k8s/namespace.yaml`) and mounted as an environment variable — replace the base64 value with your own before any real deployment.
- `banking-service` enforces that only the account owner or an ADMIN can view/modify a given account (checked in `AccountService`), independent of the coarse-grained role check on the endpoint.
- This is a learning/reference project. Before production use: rotate/secure the JWT secret via a vault, replace H2 with a persistent database (e.g. PostgreSQL), add HTTPS/TLS termination, and add refresh-token/token-revocation support.

## Tech Stack

- Java 17, Spring Boot 3.2.5
- Spring Web, Spring Data JPA, Spring Security, Bean Validation
- H2 (in-memory)
- JJWT (io.jsonwebtoken) 0.11.5
- Lombok
- Maven multi-module build
- Docker (multi-stage builds)
- Kubernetes (Deployment, Service, HPA autoscaling/v2, Ingress)
