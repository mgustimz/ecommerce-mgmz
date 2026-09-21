# AGENTS.md

Repo-specific notes for AI agents working in this monorepo.

## Commit Policy

- **The human commits and pushes.** The AI must never stage, commit, or push changes.
- The AI leaves all changes unstaged after every task so the human can review before committing.

## Roadmap Updates

- `ROADMAP.md` is the single source of truth for project progress.
- **After finishing a phase or any meaningful step, the AI must update `ROADMAP.md`** to reflect the new state:
  - Mark completed items with ✅.
  - Add new phases or items if scope changes.
  - Promote the next planned phase to 🟡 (in progress) when work starts.
  - Use status emojis: ✅ complete, 🟡 in progress, ⏳ planned.
  - Use effort tags: `S` (≤ half day), `M` (1-3 days), `L` (> 3 days).
  - Include a short "Why this matters" line per phase.
- Do not wait until the end of a long session to update the roadmap. Update it as you go.

## Layout

- `apps/api` - Spring Boot backend (Java 17, Maven, Flyway). Entry: `apps/api/src/main/java/com/example/ecommercemgmz/EcommerceMgmzApplication.java`.
- `apps/web` - Next.js customer storefront on port `3000`. App Router.
- `apps/admin` - Next.js admin dashboard on port `3001`. App Router.
- `packages/api-client` - shared typed fetch client used by both Next.js apps.
- `packages/shared` - shared frontend utils (currency formatter, session types).

`pnpm-workspace.yaml` includes `apps/*` and `packages/*`.

## Dev Commands

Root scripts in `package.json`:

```bash
pnpm install          # install workspace deps
pnpm dev:api          # backend on :8080
pnpm dev:web          # storefront on :3000
pnpm dev:admin        # admin on :3001
pnpm typecheck        # tsc --noEmit for every workspace package
pnpm build            # tsc for packages + next build for apps
pnpm lint             # tsc --noEmit (no real linter configured)
```

Backend only:

```bash
cd apps/api && ./mvnw test
cd apps/api && ./mvnw spring-boot:run
```

Use `./mvnw`, not a globally installed Maven.

## Database

Local PostgreSQL 16 must be running on `localhost:5432` with:

- user: `postgres`
- password: `postgres`
- database: `ecommerce_mgmz`

Two ways to run it:

1. Apple Container (see README):

   ```bash
   container volume create ecommerce-mgmz-postgres-data
   container run --name ecommerce-mgmz-postgres --detach --publish 5432:5432 \
     --env-file .env --env PGDATA=/var/lib/postgresql/data/pgdata \
     --volume ecommerce-mgmz-postgres-data:/var/lib/postgresql/data \
     postgres:16-alpine
   ```

   Note: PGDATA must be `/var/lib/postgresql/data/pgdata`, not the volume root, because Apple Container named volumes include a `lost+found` directory and Postgres refuses that as a data directory.

2. Docker Compose: `docker compose up -d postgres`.

Flyway auto-migrates on backend startup. If you change entities, add a new `V*__*.sql` in `apps/api/src/main/resources/db/migration` rather than mutating `V1`.

## Environment

- `.env.example` is the template; copy to `.env` for local dev. Frontend also reads `.env.local` (template: `.env.local.example`).
- `NEXT_PUBLIC_API_BASE_URL` defaults to `http://localhost:8080/api`.
- `.env` is git-ignored. Never commit secrets; `.env.example` values are local-development placeholders.

## API Auth Model

- Stateless JWT. Backend issues a token on `/api/auth/login` and `/api/auth/register`.
- `AuthResponse` includes `role`: `CUSTOMER` or `ADMIN`.
- Storefront persists token at `localStorage["mgmz_session"]` via `apps/web/src/lib/session.ts`.
- Admin persists token at `localStorage["mgmz_admin_session"]` via `apps/admin/src/lib/session.ts`.
- Backend CORS allows `http://localhost:3000` and `http://localhost:3001` (configured in `apps/api/src/main/java/com/example/ecommercemgmz/config/SecurityConfig.java`).
- Protected customer routes are wrapped in `<CustomerAuthGuard>` (redirects to `/login`); admin routes in `<AdminAuthGuard>` (redirects to `/login`).
- `JwtAuthenticationFilter` clears the security context on invalid tokens (does not return 401 immediately) so public endpoints still work when a stale token is sent.
- Passwords must contain uppercase, lowercase and a digit and may not match a common-password blocklist (see `AuthService.validatePassword`).
- `/api/auth/login|register|forgot-password` are rate-limited per IP by `AuthRateLimitFilter` (fixed window, defaults in `application.yaml`, in-memory counters; multi-instance needs Redis). Exceeding returns 429 + `Retry-After`.
- Password reset: `POST /api/auth/forgot-password` always returns a neutral message (no user enumeration); tokens are SHA-256 hashed at rest, 30-min expiry, single-use, invalidated on new request. Reset links are logged server-side via `LoggingEmailSender` (`app.mail.mode=log`) because there is no SMTP provider yet.

## Security Invariants

- **CSRF protection is intentionally disabled.** The API is stateless and authenticates only via the `Authorization: Bearer <jwt>` header (read by `JwtAuthenticationFilter`). Browsers do not auto-attach `Authorization` headers cross-origin, and the explicit CORS allow-list in `SecurityConfig` is the actual CSRF defense. The anonymous cart cookie is `SameSite=Lax` and not sent on cross-site POSTs.
- **Do not switch to cookie-based auth** (JSESSIONID, HttpOnly auth cookies, etc.) without also re-enabling CSRF.
- **Do not call `allowCredentials(true)`** with a wildcard CORS origin. CORS with credentials and a wildcard is rejected by browsers, but loosening this later would re-introduce CSRF exposure.
- **Keep the CORS allow-list explicit** (`http://localhost:3000`, `http://localhost:3001`). Adding a new frontend host means updating both `corsConfigurationSource` and the documented dev hosts.
- See `SECURITY.md` for the full threat model.

## Shared API Client

Always extend `packages/api-client/src/index.ts` when adding or changing a backend endpoint. Both apps consume it; do not call `fetch` directly from pages.

`next.config.ts` in each Next.js app sets `transpilePackages: ["@mgmz/api-client", "@mgmz/shared"]` so the packages build correctly under Next/Turbopack.

## Conventions That Differ From Defaults

- Both Next.js apps use Tailwind v4 via `@tailwindcss/postcss`; styling entry is `src/app/globals.css` with `@import "tailwindcss";`.
- App Router only (no `pages/`).
- API request bodies are typed via inline types in the api-client (e.g. `ProductInput`, `AddressInput`, `CategoryInput`); do not pass `unknown` to the client.
- Cart delete endpoint returns 204; the storefront calls `cart.get` again instead of expecting a body.
- Coupons: discount math and validation only happen server-side in `coupon/CouponService` (`quote`); the frontend never computes discounts. Redemption is recorded per order at checkout via `coupon_redemptions` (per-customer single use + global cap enforced in `quote`). `orders.coupon_id` / `orders.discount_amount` feed `OrderResponse.couponCode` / `discountAmount`.
- Admin product form uses backend `ProductShippingCategory` enum values (lowercase, snake_case where applicable: `electronic`, `food_and_drink`, etc.), not human labels.
- Backend status enums are strings, not booleans (`ProductStatus`: `ACTIVE|DRAFT|ARCHIVED`; `OrderStatus`: `PENDING_PAYMENT|PAID|PROCESSING|SHIPPED|COMPLETED|CANCELLED`).
- Stock is deducted at checkout (that deduction is the reservation) and released when a payment expires or the order is cancelled. Always mutate stock through the atomic tools in `ProductRepository` (`deductStock` / `restoreStock`); a check-then-set read-modify-write has an oversell race and is rejected in review. `PaymentExpiryScheduler` sweeps expired pending orders every 60s (in-process; add a distributed lock for multi-instance deploys).

## Lombok Notes

- Lombok is configured in `apps/api/pom.xml` (dependency + annotation processor).
- **Lombok does NOT propagate `@Value` from field to constructor parameter.** For fields using `@Value("${...}")`, declare an explicit constructor (e.g. `JwtService`, `OrderService`, `AdminUserSeeder`).
- JPA entities use `@Getter @Setter @NoArgsConstructor(access = AccessLevel.PROTECTED)` to keep the JPA-required protected no-arg constructor.

## Verification Order

Before claiming a change is done, run from repo root:

```bash
pnpm typecheck
pnpm build
cd apps/api && ./mvnw test
```

## Build Artifacts To Ignore

`apps/*/node_modules`, `apps/*/.next`, `apps/api/target`, `node_modules`, `tsconfig.tsbuildinfo`, `apps/api/HELP.md` are all generated; do not commit them. The repo's `.gitignore` already covers them but Next.js sometimes touches tracked `next-env.d.ts` files during build - if so, restore from HEAD before committing.

## Known Gaps

- Admin product list endpoint is `GET /api/admin/products` (added in this repo). It returns all statuses, unlike the public `/api/products` which is active-only.
- Admin order detail uses `GET /api/orders/{id}` with an admin JWT (the order controller allows admins through the same path).
- Checkout fetches real rates from `POST /api/shipping/rates` and lets the customer pick; falls back to `REG` on error. Without Biteship keys configured, the backend returns local `REG`/`EXP` rates.
- Admin inventory list page is a placeholder; the endpoint `GET /api/admin/inventory-movements` exists but is not yet consumed in the UI.

## Progress Tracking

- See `ROADMAP.md` for the live roadmap.
- Update it after every meaningful step, not at the end of a session.
