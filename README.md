# ShopMart

A demo full-stack e-commerce app: Spring Boot 3.2 (Java 17) REST API + React 18 (Vite) frontend, backed by MySQL 8.

This project intentionally ships with **6 known bugs** (see below) for code-review / debugging practice. They are not fixed here on purpose.

## Stack

- Backend: Java 17, Spring Boot 3.2, Spring Data JPA, Spring Security (stateless JWT), Maven
- Database: MySQL 8 (schema `shopmart`, user `shopmart` / password `shopmart123`)
- Frontend: React 18, Vite, Axios, React Router v6, plain CSS
- Ports: backend `:8080`, frontend `:5173`

## Prerequisites

- JDK 17, Maven 3.9+
- Node 18+, npm
- A running local MySQL 8 instance with a `shopmart` schema and a `shopmart` user (password `shopmart123`) that has privileges on it, e.g.:

```sql
CREATE DATABASE shopmart;
CREATE USER 'shopmart'@'localhost' IDENTIFIED BY 'shopmart123';
GRANT ALL PRIVILEGES ON shopmart.* TO 'shopmart'@'localhost';
```

The backend creates/updates its own tables on startup (`spring.jpa.hibernate.ddl-auto=update`) and seeds `data.sql` (12 products + an admin and a customer user) idempotently via `INSERT IGNORE`.

## Running the backend

```bash
cd shopmart-api
mvn spring-boot:run
```

API is available at `http://localhost:8080/api`. Run the test suite with `mvn test`.

## Running the frontend

```bash
cd shopmart-ui
npm install
npm run dev
```

App is available at `http://localhost:5173`. Run the test suite with `npm run test`.

## Seed accounts

| Email | Password | Role |
|---|---|---|
| admin@shopmart.com | admin123 | ADMIN |
| customer@shopmart.com | customer123 | CUSTOMER |

12 seed products span Electronics, Clothing, and Books.

## API summary

| Method | Path | Auth | Notes |
|---|---|---|---|
| POST | /api/auth/register | public | 201, returns JWT |
| POST | /api/auth/login | public | 200, returns JWT |
| GET | /api/products?page=&size=&category= | public | Paginated list |
| GET | /api/products/{id} | public | Single product |
| POST | /api/cart/items | CUSTOMER | Add/increment a cart line |
| GET | /api/cart | CUSTOMER | Line items + subtotal/discount/grandTotal |
| DELETE | /api/cart/items/{id} | CUSTOMER (own item) | Remove a cart line |
| POST | /api/orders | CUSTOMER | Place order from current cart, clears cart |
| GET | /api/orders, /api/orders/{id} | CUSTOMER / ADMIN | Order history / detail |
| PATCH | /api/orders/{id}/status | ADMIN | Change order status |

## Known intentional bugs

The backend test suite asserts *correct* behavior, so the tests corresponding to these bugs are **expected to fail** out of the box — that's intentional, not a regression. Do not fix these unless that's specifically the task at hand.

| # | Bug | Location | Effect |
|---|---|---|---|
| 1 | Pagination off-by-one | `ProductController.getProducts()` — `PageRequest.of(page + 1, size)` | Every page request is shifted forward by one; `page=0` returns page 2's data. |
| 2 | Double discount | `CartService.getCart()` | The 10% loyalty discount is subtracted from the subtotal twice, understating `grandTotal` by an extra 10%. |
| 3 | Missing `@Transactional` | `OrderService.placeOrder()` | Order creation and cart-clearing are two separate, non-atomic writes — a failure between them can leave the cart and order history inconsistent. |
| 4 | NPE on missing product | `ProductController.getProductById()` — `.get()` on an `Optional` | Requesting a nonexistent product throws an unhandled exception (mapped to a 500) instead of a clean 404. |
| 5 | No order-status transition validation | `OrderService.updateStatus()` | Any status can transition to any other status, including `DELIVERED → PENDING`. |
| 6 | Wrong HTTP status | `CartController.addItem()` | Returns `200 OK` instead of `201 Created` for a newly created cart line. |

Each is marked in the source with a `// BUG (intentional): ...` comment.
