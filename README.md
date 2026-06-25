# ecommerce-mgmz

Single-store ecommerce monorepo with a Spring Boot API, customer storefront, and admin dashboard.

## Apps

- `apps/api` - Spring Boot backend
- `apps/web` - Next.js customer storefront on port `3000`
- `apps/admin` - Next.js admin dashboard on port `3001`
- `packages/api-client` - shared typed API client
- `packages/shared` - shared frontend utilities and types

## Local Setup

Install dependencies:

```bash
pnpm install
```

Start PostgreSQL with Apple Container:

Create a local `.env` from `.env.example` first. The example values are for local development only; never commit real secrets.

```bash
container volume create ecommerce-mgmz-postgres-data
container run \
  --name ecommerce-mgmz-postgres \
  --detach \
  --publish 5432:5432 \
  --env-file .env \
  --env PGDATA=/var/lib/postgresql/data/pgdata \
  --volume ecommerce-mgmz-postgres-data:/var/lib/postgresql/data \
  postgres:16-alpine
```

Verify the database connection:

```bash
PGPASSWORD="$DATABASE_PASSWORD" psql -h localhost -p 5432 -U "$DATABASE_USERNAME" -d ecommerce_mgmz
```

Stop or start the database later:

```bash
container stop ecommerce-mgmz-postgres
container start ecommerce-mgmz-postgres
```

Alternative Docker setup:

```bash
docker compose up -d postgres
```

Run the API:

```bash
pnpm dev:api
```

Run the storefront:

```bash
pnpm dev:web
```

Run the admin dashboard:

```bash
pnpm dev:admin
```

## Verification

```bash
pnpm typecheck
pnpm build
cd apps/api && ./mvnw test
```
