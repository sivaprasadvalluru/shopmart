# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

### Backend (run from project root)

```bash
mvn clean verify          # compile + run all tests
mvn spring-boot:run       # start API on :8080
mvn test -pl . -Dtest=CartControllerTest          # single test class
mvn test -pl . -Dtest=CartControllerTest#addItemToCart  # single test method
```

### Frontend (run from `frontend/`)

```bash
npm install
npm run dev       # dev server on :5173
npm run build     # tsc + vite build (type-checks first)
npm test          # vitest run (one-shot)
```

## Architecture

This is a two-process app: a Spring Boot API on `:8080` and a Vite dev server on `:5173`. CORS is configured in `WebConfig` to allow the frontend origin.

### Backend (`src/main/java/com/shopmart/`)

- **`model/`** — JPA entities: `Category` and `Product` (many-to-one to Category, `EAGER` fetch).
- **`repository/`** — Spring Data JPA interfaces; `ProductRepository` adds `findByCategoryId`.
- **`controller/`** — `CategoryController`, `ProductController` (optional `?categoryId` filter), `CartController` (reads `X-Session-Id` header, falls back to a hardcoded `"default"` session).
- **`cart/`** — Cart is **in-memory only** (`ConcurrentHashMap<String, Cart>` inside `CartService`). It is not persisted; restarting the server clears all carts.

### Frontend (`frontend/src/`)

- **`api/client.ts`** — All fetch calls live here. On first load it generates and stores a UUID in `localStorage` as `shopmart-session-id` and sends it as `X-Session-Id` on every request.
- **`App.tsx`** — Three routes: `/` (ProductList), `/products/:id` (ProductDetail), `/cart` (CartPage).
- **`components/Navbar.tsx`** — Polls `GET /api/cart` every 5 s to keep the badge count fresh.

### Test setup

Tests use H2 in-memory instead of MySQL. The test `application.properties` sets `spring.sql.init.data-locations=classpath:test-data.sql` — this is intentional to avoid the main `data.sql` (which uses MySQL-specific `INSERT IGNORE`) being picked up on the classpath.

## Intentional teaching defects — do not fix without being asked

Two bugs are left in deliberately for course exercises (Steps 9 and 10):

1. **Cart quantity** — `CartService.addItem` and `updateItem` accept 0 and negative quantities without validation. The backend tests assert this behaviour passes.
2. **Empty-cart checkout** — `CartPage.tsx` renders the "Place Order" button and allows checkout even when `cart.items` is empty. The frontend test asserts the button is always present.
