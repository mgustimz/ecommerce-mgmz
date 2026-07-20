# AGENTS.md

Repo-specific notes for AI agents working in this monorepo.

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

## Shared API Client

Always extend `packages/api-client/src/index.ts` when adding or changing a backend endpoint. Both apps consume it; do not call `fetch` directly from pages.

`next.config.ts` in each Next.js app sets `transpilePackages: ["@mgmz/api-client", "@mgmz/shared"]` so the packages build correctly under Next/Turbopack.

## Conventions That Differ From Defaults

- Both Next.js apps use Tailwind v4 via `@tailwindcss/postcss`; styling entry is `src/app/globals.css` with `@import "tailwindcss";`.
- App Router only (no `pages/`).
- API request bodies are typed via inline types in the api-client (e.g. `ProductInput`, `AddressInput`, `CategoryInput`); do not pass `unknown` to the client.
- Cart delete endpoint returns 204; the storefront calls `cart.get` again instead of expecting a body.
- Admin product form uses backend `ProductShippingCategory` enum values (lowercase, snake_case where applicable: `electronic`, `food_and_drink`, etc.), not human labels.
- Backend status enums are strings, not booleans (`ProductStatus`: `ACTIVE|DRAFT|ARCHIVED`; `OrderStatus`: `PENDING_PAYMENT|PAID|PROCESSING|SHIPPED|COMPLETED|CANCELLED`).

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
- Checkout currently submits `REG` or `EXP` shipping codes directly; the shipping rate selector UI is not yet wired to `POST /api/shipping/rates`.
- Admin inventory list page is a placeholder; the endpoint `GET /api/admin/inventory-movements` exists but is not yet consumed in the UI.
