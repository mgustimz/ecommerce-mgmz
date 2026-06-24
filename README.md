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

Start PostgreSQL with Docker:

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
