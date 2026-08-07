# SECURITY

Threat model and security invariants for this project. Read this before touching auth, sessions, cookies, or CORS.

## Overview

- Stateless Spring Boot API (`apps/api`) + Next.js storefront (`apps/web`) + Next.js admin (`apps/admin`).
- Auth is JWT in an `Authorization: Bearer <token>` header, stored in `localStorage` on the client.
- No server-side HTTP sessions, no JSESSIONID, no auth cookies.

## Auth Model

- `/api/auth/login` and `/api/auth/register` issue a JWT (`AuthResponse.token`).
- The JWT payload carries `uid`, `sub` (email), and `role` (`CUSTOMER` | `ADMIN`).
- The client sends it as `Authorization: Bearer <token>` on every authenticated request.
- `JwtAuthenticationFilter` parses the header, populates the `SecurityContext`, and clears the context on an invalid/expired token (so public endpoints keep working with a stale token).
- Storefront session: `localStorage["mgmz_session"]`. Admin session: `localStorage["mgmz_admin_session"]`.

## CSRF Stance

**CSRF protection is intentionally disabled** in `SecurityConfig`.

Why it is safe in this design:

- Requests authenticate via the `Authorization` header, which browsers **do not auto-attach** to cross-origin requests. An attacker cannot make the victim's browser send it.
- Any request with a custom header (or a non-simple method) triggers a CORS preflight, and the CORS allow-list refuses origins other than `http://localhost:3000` and `http://localhost:3001`.
- The anonymous cart cookie is `SameSite=Lax` and is not sent on cross-site `POST`s.

Do **not** re-enable CSRF tokens "just to be safe" — for a header-auth, CORS-gated API that is security theater.

## Security Invariants

These must never be violated:

1. **No cookie-based auth.** Do not switch to JSESSIONID, HttpOnly auth cookies, or session-based identity without also re-enabling CSRF protection.
2. **No `allowCredentials(true)` with a wildcard origin.** If credentials are ever allowed, the origin allow-list must remain explicit.
3. **Keep the CORS allow-list explicit.** New frontend hosts require updating `corsConfigurationSource` in `SecurityConfig`.
4. **Never render product/user content as raw HTML** in the storefront/admin. React's default escaping is the XSS defense.
5. **Passwords use BCrypt** (`BCryptPasswordEncoder`). Do not change to a weaker hasher.

## Threat Model

### In scope / mitigated

| Threat | Mitigation |
|---|---|
| Cross-site request forgery | Header-auth + CORS preflight + `SameSite=Lax` cookie (see above) |
| Stored/reflected XSS | React escapes text by default; do not use `dangerouslySetInnerHTML` |
| SQL injection | JPA/parameterized queries; no string-built SQL |
| Broken access control | `SecurityFilterChain` role rules (`/api/admin/**` → `ADMIN`) |
| Stolen JWT | Token lifetime (`JWT_EXPIRATION_MINUTES=1440`); future: refresh/revocation |
| Inventory/stock races | `@Transactional` + stock validation in cart/order services |

### Out of scope for this iteration

- Rate limiting / DDoS / credential stuffing (login has no rate limit yet).
- Email-based password reset or verification (no email infrastructure).
- Refresh tokens / JWT revocation.
- 2FA / TOTP for admin.
- Real payment gateway (PSP) — Phase 7 in `ROADMAP.md`.
- Audit logs / SIEM.

## When You Change Auth, Sessions, or CORS

1. Re-read this file and the `Security Invariants` in `AGENTS.md`.
2. Update `SecurityConfig.java` and the CORS source together; keep the allow-list explicit.
3. If you introduce cookie-based auth, re-enable CSRF and update both this file and `AGENTS.md`.
4. Run the full verification: `pnpm typecheck`, `pnpm build`, `cd apps/api && ./mvnw test`.
