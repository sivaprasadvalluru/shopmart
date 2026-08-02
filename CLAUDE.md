# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

ShopMart is a demo full-stack e-commerce app: Spring Boot 3.2 (Java 17) REST API + React 18 (Vite) frontend, backed by MySQL 8 in dev and H2 (MySQL-compatibility mode) in tests.

**This repo intentionally ships with 6 known bugs**, seeded for code-review/debugging practice. They are documented with `// BUG (intentional): ...` comments at the source and in the table below. **Do not fix them unless a task specifically asks you to** — the corresponding backend tests are written to assert correct behavior and are expected to fail out of the box; that's by design, not a regression you introduced.

| # | Bug | Location | Effect |
|---|---|---|---|
| 1 | Pagination off-by-one | `ProductController.getProducts()` — `PageRequest.of(page + 1, size)` | Every page request is shifted forward by one; `page=0` returns page 2's data. |
| 2 | Double discount | `CartService.getCart()` | The 10% loyalty discount is subtracted from the subtotal twice, understating `grandTotal` by an extra 10%. |
| 3 | Missing `@Transactional` | `OrderService.placeOrder()` | Order creation and cart-clearing are two separate, non-atomic writes — a failure between them can leave the cart and order history inconsistent. |
| 4 | NPE on missing product | `ProductController.getProductById()` — `.get()` on an `Optional` | Requesting a nonexistent product throws an unhandled exception (mapped to a 500) instead of a clean 404. |
| 5 | No order-status transition validation | `OrderService.updateStatus()` | Any status can transition to any other status, including `DELIVERED → PENDING`. |
| 6 | Wrong HTTP status | `CartController.addItem()` | Returns `200 OK` instead of `201 Created` for a newly created cart line. |

## Commands

### Backend (`shopmart-api/`)

```bash
cd shopmart-api
mvn spring-boot:run          # run API on :8080 (needs local MySQL, see Prerequisites below)
mvn test                     # run full backend test suite (uses H2, no MySQL needed)
mvn test -Dtest=CartServiceTest            # run a single test class
mvn test -Dtest=CartServiceTest#getCart_appliesDiscountOnce   # run a single test method
```

Backend tests run against an in-memory H2 database (`application-test.properties`) in MySQL-compatibility mode, so `mvn test` never requires a running MySQL instance.

### Frontend (`shopmart-ui/`)

```bash
cd shopmart-ui
npm install
npm run dev        # dev server on :5173
npm run build
npm run test        # vitest run
```

### Prerequisites for running the backend against real MySQL

A local MySQL 8 instance with schema/user matching `application.properties` (`shopmart` schema, `shopmart`/`shopmart123`). `spring.jpa.hibernate.ddl-auto=update` creates/updates tables on startup, and `data.sql` idempotently seeds 12 products plus an admin and customer user (see README for seed credentials).

## Architecture

### Backend layering

`controller` → `service` → `repository` (Spring Data JPA), with `dto` records for request/response shapes and `model.entity` for JPA entities. Controllers stay thin; **the service layer owns business logic** — put new validation/calculation logic in services, not controllers.

- **Auth**: stateless JWT. `JwtAuthFilter` (a `OncePerRequestFilter`-style filter) runs before Spring's `UsernamePasswordAuthenticationFilter` and populates the security context from the `Authorization: Bearer` header; `JwtTokenProvider` issues/validates tokens; `UserDetailsServiceImpl`/`UserPrincipal` bridge JPA `User` entities into Spring Security. `SecurityConfig` is the single source of truth for which routes are public (`/api/auth/**`, `GET /api/products/**`) vs. authenticated; everything else requires a valid JWT. No server-side sessions.
- **Errors**: all exceptions are centralized in `GlobalExceptionHandler` (`@RestControllerAdvice`), mapping domain exceptions (`ResourceNotFoundException`, `InvalidRequestException`, `EmailAlreadyExistsException`, Spring Security's `AccessDeniedException`/`BadCredentialsException`, validation errors) to consistent `ErrorResponse` JSON bodies with the right HTTP status. Add new domain exceptions here rather than handling status codes ad hoc in controllers.
- **Cart → Order flow**: `CartService` manages `CartItem` rows per user (add/list/remove). `OrderService.placeOrder()` reads the user's cart, snapshots each line into an `OrderItem` (capturing `priceAtPurchase` so historical orders are immune to later price changes), decrements `Product.stockQuantity`, saves the `Order`, and clears the cart — this is the non-atomic sequence referenced in bug #3 above.
- **Authorization checks live in services, not controllers**: e.g. `CartService.removeItem` and `OrderService.getOrderById` compare the resource owner against the authenticated `User` and throw `AccessDeniedException` for cross-user access, rather than filtering at the repository/query level.

### Frontend structure

- All backend calls go through `src/api/*.js` helpers built on a single shared `axiosInstance` (`src/api/axiosInstance.js`), which attaches the JWT from `localStorage` to every outgoing request via an interceptor. Don't call `axios` directly from components/pages.
- Auth state (token/email/role) lives in `AuthContext` (`src/context/AuthContext.jsx`), backed by `localStorage`, and is consumed via the `useAuth()` hook. `ProtectedRoute` gates `/cart`, `/orders`, `/orders/:id` on `isAuthenticated`.
- Routing is a flat `Routes` table in `App.jsx`; there's no nested/layout routing beyond the persistent `Navbar`.
- Functional components with hooks only — no class components.

## Coding conventions

From `docs/standards/`:

- **Java services**: use `@Transactional` on multi-write service methods; never call `Optional.get()` without `orElseThrow`; service layer owns business logic, controllers stay thin. `CartService.getCart()` is called out as sensitive — review any change to it carefully, and run `mvn test` after touching it.
- **React UI**: Axios calls go through `src/api/` helpers only; get the JWT from auth context, not ad hoc `localStorage` reads; functional components with hooks, no class components; test files are named `*.test.jsx` and live beside the component they test.
