# ROADMAP

Single-store ecommerce monorepo. From foundation to a fully featured store.

## Legend

- ✅ Complete
- 🟡 In progress
- ⏳ Planned
- Effort: `S` (≤ half day), `M` (1-3 days), `L` (> 3 days)

## Current State (as of now)

- Backend: 11 Spring Boot modules, PostgreSQL via Flyway, JWT auth, CORS configured for `:3000` and `:3001`.
- Customer: register, login, browse, search, cart, checkout, orders, addresses, store credits-style flow.
- Admin: dashboard, products, categories, orders, inventory list, auth, logout.
- Shared: typed API client, currency formatter, design tokens.
- Infra: pnpm workspace, Docker/Apple Container PostgreSQL, AGENTS.md notes.

---

## Phase 0: Foundation ✅

- [x] Monorepo layout: `apps/api`, `apps/web`, `apps/admin`, `packages/*`
- [x] pnpm workspace + root scripts (`dev:api`, `dev:web`, `dev:admin`, `build`, `typecheck`)
- [x] Spring Boot skeleton with Flyway migrations
- [x] Next.js App Router apps for storefront and admin

**Why this matters**: foundation for everything else. Wrong structure here is expensive later.

---

## Phase 1: Customer MVP ✅

- [x] Auth: register, login, JWT-protected endpoints
- [x] Categories + products list/detail (public)
- [x] Cart: add / update / remove
- [x] Checkout: address, shipping method, payment method
- [x] Orders: customer list / detail / cancel
- [x] Customer addresses: CRUD + default

**Why this matters**: the core shopping loop. Without this, no revenue.

---

## Phase 2: Admin MVP ✅

- [x] Admin auth + role check
- [x] Dashboard summary + low-stock widget
- [x] Products: list, create, edit, archive
- [x] Categories: list, create, edit, delete
- [x] Orders: list, detail, status update, cancel
- [x] Customer auth guard for protected storefront pages
- [x] Admin auth guard for protected admin pages
- [x] 401 auto-redirect to login

**Why this matters**: operators need to manage the store without touching the database.

---

## Phase 3: Storefront UI Overhaul ✅

- [x] Honda-inspired visual design: brand red `#cc1d00`, top utility bar, header, mega menu nav, 4-column footer
- [x] Reusable product card, gallery, search/filter
- [x] Home with hero, categories, featured products, reviews strip, location card

**Why this matters**: design and UX drive conversion. A clean storefront signals trust.

---

## Phase 4: Product Experience ✅ (mostly)

- [x] `originalPrice` field + discount badge on product card and detail page
- [x] `averageRating` + `reviewCount` per product (random dummy data)
- [x] Multiple product images with clickable gallery + prev/next nav
- [x] Lombok applied to entities, services, controllers

**Why this matters**: reviews + discounts are the highest-ROI trust signals on a product page.

---

## Phase 5: Trust & Security 🟡 (Next)

- [ ] Forgot password / reset password flow
- [ ] Email verification on register
- [x] Customer profile page (edit name / phone)
- [ ] Stronger password rules
- [ ] Rate limiting on auth endpoints
- [ ] Admin 2FA (TOTP)

**Effort**: M-L

**Why this matters**: trust is the conversion rate's biggest multiplier. Verified users recover passwords; verified emails enable transactional notifications; 2FA protects the admin from account takeover.

---

## Phase 6: Real Shopping 🟡 (in progress)

- [x] Guest cart: add to cart without login, persist across login via anonymous cart + merge
- [x] Hide "My Account" / "Cart" from the logged-out header (guests see Login / Register)
- [ ] Real shipping rate selection in checkout (`POST /api/shipping/rates`)
- [ ] Image upload (replace URL pasting) with local storage or object store
- [ ] Coupons / promo codes
- [ ] Product variants (size, color, material)
- [ ] Stock reservation on checkout (release on payment timeout)
- [ ] Cart abandonment tracking

**Effort**: L

**Why this matters**: real shipping is essential; image upload unlocks seller-led catalogs; coupons drive conversion; variants are table-stakes for non-digital SKUs. A guest cart removes the biggest conversion blocker - "I had to sign up just to save a product."

---

## Phase 7: Payments ⏳

- [ ] Real payment gateway (Midtrans / Xendit / Stripe) for `BANK_TRANSFER`, `VIRTUAL_ACCOUNT`, `EWALLET`, `QRIS`
- [ ] Payment webhook endpoint
- [ ] Admin "Simulate paid" / "Expire" buttons in order detail (endpoints exist; UI missing)
- [ ] Refund / partial refund flow
- [ ] Invoice generation (PDF)

**Effort**: L

**Why this matters**: without a real PSP, this is a demo. The admin simulate-paid buttons close the loop for testing the order lifecycle.

---

## Phase 8: Reviews 2.0 ⏳

- [ ] Customer can write a review on a delivered order
- [ ] One review per customer per product
- [ ] Star rating + comment + optional photos
- [ ] Helpful votes
- [ ] Admin moderation (approve / hide)

**Effort**: M

**Why this matters**: reviews only feel real when customers can write them. Closing the loop drives retention and trust.

---

## Phase 9: Discovery ⏳

- [ ] Search: Postgres `tsvector` full-text (name, description, sku)
- [ ] Category facets + price range filter
- [ ] Wishlist
- [ ] Recently viewed
- [ ] Related products / "you might like"
- [ ] Sort options (popularity, newest, price)

**Effort**: M

**Why this matters**: customers who browse more buy more. Search and recommendations are the difference between a brochure and a store.

---

## Phase 10: Operations & Analytics ⏳

- [ ] Admin analytics with charts (revenue, conversion, top products, traffic)
- [ ] Export orders / products / customers to CSV
- [ ] Inventory forecasting (low-stock alerts)
- [ ] Multi-warehouse
- [ ] Bulk product update (CSV import)

**Effort**: L

**Why this matters**: operators need data, not just CRUD. Charts and exports make a backend feel professional.

---

## Phase 11: Marketing ⏳

- [ ] Email notifications: order confirmation, payment, shipping, delivery
- [ ] Abandoned cart recovery email
- [ ] Loyalty / rewards points
- [ ] Banners / promo slots on home
- [ ] Affiliate / referral links

**Effort**: L

**Why this matters**: email is the highest-ROI marketing channel for ecommerce. A single abandoned-cart flow can pay for the whole stack.

---

## Phase 12: Platform Polish ⏳

- [ ] Internationalization (i18n) - at minimum English + Indonesian
- [ ] SEO: sitemap.xml, robots.txt, OpenGraph, structured data
- [ ] PWA / mobile installable
- [ ] Accessibility audit (WCAG AA)
- [ ] Performance: image CDN, ISR caching, code splitting

**Effort**: L

**Why this matters**: SEO and PWA are the difference between "exists" and "discoverable". Accessibility is non-negotiable for a real store.

---

## Phase 13: Production Readiness ⏳

- [ ] HTTPS / reverse proxy (Caddy or Nginx)
- [ ] Logging and monitoring (Sentry + Grafana)
- [ ] Database backups
- [ ] CI/CD (GitHub Actions)
- [ ] Docker images for backend + web + admin
- [ ] Production environment variables / secrets management
- [ ] Rate limiting + WAF
- [ ] Staging environment

**Effort**: L

**Why this matters**: none of the above matters if the site is down or compromised. Production hardening is the last step.

---

## End Goal: "Rich Feature" Definition

A complete single-store ecommerce that:

1. Lets customers discover products, read/write reviews, save wishlists, checkout with real payments and shipping.
2. Lets operators manage catalog, inventory, orders, customers, and analytics from one admin.
3. Sends transactional email for every order state.
4. Has search, SEO, and PWA so customers can find the store.
5. Is monitored, backed up, and deployable with a single command.
