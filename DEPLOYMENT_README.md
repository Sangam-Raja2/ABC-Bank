# Deployment setup — one-time steps

## 1. Repo layout
Drop these into your repo root (paths matter):
```
Dockerfile
.dockerignore
.github/workflows/ci-cd.yml
k8s/configmap.yaml
k8s/service.yaml
k8s/deployment.yaml
k8s/ingress.yaml
k8s/secret.example.yaml   (template only — do NOT commit real secrets)
```
The Java files (`JwtAuthenticationFilter`, `JwtUserMapper`, `CurrentUserProvider`) go into
`src/main/java/com/sangam/abcbank/security/`.

## 2. Create the real Kubernetes Secret (never commit this)
```bash
kubectl create secret generic abc-bank-secret \
  --from-literal=JWT_SECRET=your-real-secret \
  --from-literal=DB_PASSWORD=your-real-password
```

## 3. Add one repo secret for CI to reach your cluster
Settings → Secrets and variables → Actions → New repository secret:
- `KUBE_CONFIG` — base64 of your kubeconfig: `cat ~/.kube/config | base64 -w0`

No registry secret needed — GHCR auth uses the built-in `GITHUB_TOKEN`.
If your cluster is private (e.g. behind a VPN/self-hosted), you'll instead want
a self-hosted GitHub Actions runner with network access to it — say the word
and I'll adjust the workflow for that.

## 4. What happens on push
- Any push/PR → build + run tests (`build-and-test` job).
- Push to `main` only → also:
  1. Builds the Docker image, pushes to `ghcr.io/<your-org>/abc-bank` tagged
     both `latest` and the commit SHA.
  2. Applies the k8s manifests (idempotent).
  3. Points the Deployment at the new SHA-tagged image and waits for the
     rollout to finish, so a broken image fails the pipeline rather than
     silently going live.

## 5. Adjust before first run
- `k8s/deployment.yaml`: image name if your GitHub org/repo differs from
  `ghcr.io/sangam-raja2/abc-bank`.
- `k8s/ingress.yaml`: real domain + your ingress controller's class name.
- Health probe paths (`/actuator/health/readiness` / `/liveness`) assume
  Spring Boot Actuator with health groups enabled. If you're not using
  Actuator, swap these for a simple endpoint you already expose, or add:
  ```yaml
  management.endpoint.health.probes.enabled: true
  management.health.livenessstate.enabled: true
  management.health.readinessstate.enabled: true
  ```
