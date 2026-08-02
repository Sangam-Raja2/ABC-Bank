# emi-service — ABC Bank

Handles EMI conversion ("EMI transition") for credit card and loan balances, a standalone
EMI calculator, and CIBIL score checks. Designed as a sibling module to `user-service`,
`banking-service`, `loan-service`, and `credit-card-service` in the ABC-Bank repo.

## Where this plugs in

| Service           | Port     | Responsibility                                                  |
| ------------------|----------| ----------------------------------------------------------------|
| `user-service`    | 8081     | Issues JWTs                                                     |
| `banking-service` | 8082     | Accounts, deposits, withdrawals                                 |
| `loan-service`    | 8083     | Loan lifecycle                                                  |
| `credit-card-service` | 8084     | Credit card transactions                                    |
| **`emi-service`** | **8085** | **EMI calculator, EMI transition, CIBIL score check**       |

\* Port guessed — adjust `services.credit-card-service.base-url` if different.

Auth flow is unchanged: get a JWT from `user-service`, send it as `Bearer <token>` to
`emi-service`. It validates the token locally using the same shared `jwt.secret` — no
callback to `user-service` needed.

## Endpoints

| Method | Path                          | Auth              | Purpose                                   |
|--------|-------------------------------|-------------------|--------------------------------------------|
| POST   | `/api/emi/calculator`         | open               | Compute EMI + amortization schedule        |
| POST   | `/api/emi/transition`         | CUSTOMER/ADMIN/LOAN_OFFICER/MANAGER | Convert a CC txn or loan balance to EMI |
| GET    | `/api/emi/transition/history` | authenticated      | Caller's EMI transition history            |
| POST   | `/api/emi/cibil/check`        | authenticated      | CIBIL score check (mocked, see below)      |

Swagger UI: `http://localhost:8084/swagger-ui/index.html`
H2 console: `http://localhost:8084/h2-console` (`jdbc:h2:mem:emidb`, user `sa`, pass `password`)

## Example requests

```bash
# EMI calculator
curl -X POST localhost:8084/api/emi/calculator \
  -H "Content-Type: application/json" \
  -d '{"principal": 100000, "annualInterestRate": 12.5, "tenureMonths": 12}'

# EMI transition (credit card)
curl -X POST localhost:8084/api/emi/transition \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"sourceType":"CREDIT_CARD","sourceReferenceId":"TXN123","amount":50000,"annualInterestRate":15,"tenureMonths":6}'

# CIBIL check
curl -X POST localhost:8084/api/emi/cibil/check \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"panNumber":"ABCDE1234F"}'
```

## Important caveats

1. **CIBIL integration is mocked.** Real bureau access (TransUnion CIBIL) requires a
   commercial agreement and signed customer consent per RBI/credit information company
   regulations — it can't be called directly from app code. `CibilService` is written so a
   real provider can be swapped in later behind the same interface.
2. **Downstream verification endpoints are best-guess.** `DownstreamBankingClient` calls
   `credit-card-service` at `/api/credit-cards/transactions/{id}` and `loan-service` at
   `/api/loans/{id}` to confirm a reference is valid before converting it to EMI. Update
   these paths to match your actual controllers.
3. **Not yet wired into the root Maven build.** Add `<module>emi-service</module>` to the
   repo's root `pom.xml` `<modules>` block, and add a service block to `docker-compose.yml`
   mirroring `loan-service`, to fully integrate it.
