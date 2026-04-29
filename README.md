# ShopMart — E-Commerce Demo

A teaching demo app with a Spring Boot backend and React + TypeScript frontend.

## Prerequisites

| Tool | Version |
|------|---------|
| Java | 17+ |
| Maven | 3.9+ |
| Node.js | 18+ |
| MySQL | 8+ |

## Database setup

```sql
CREATE DATABASE shopmart CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Update credentials in `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/shopmart
spring.datasource.username=<your-username>
spring.datasource.password=<your-password>
```

## Running the backend

```bash
# from the project root
mvn spring-boot:run
```

The API starts on **http://localhost:8080**. On first start, `data.sql` seeds categories and products.

## Running the frontend

```bash
cd frontend
npm install
npm run dev
```

The dev server starts on **http://localhost:5173**.

## API Overview

### Categories

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/categories` | List all categories |

### Products

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/products` | List all products |
| GET | `/api/products?categoryId={id}` | Filter by category |
| GET | `/api/products/{id}` | Get single product |

### Cart

Cart is stored in-memory per session. Pass `X-Session-Id` header to identify the session; omit it to use a shared default session.

| Method | Endpoint | Body | Description |
|--------|----------|------|-------------|
| GET | `/api/cart` | — | View cart |
| POST | `/api/cart/items` | `{"productId":1,"quantity":2}` | Add item |
| PUT | `/api/cart/items/{productId}` | `{"quantity":3}` | Update quantity |
| DELETE | `/api/cart/items/{productId}` | — | Remove item |

## Running tests

```bash
# Backend (requires no running DB — uses H2 in-memory)
mvn clean verify

# Frontend
cd frontend
npm test
```

## Known teaching defects (intentional)

These are left deliberately broken for course exercises:

1. **Cart quantity validation** — the API accepts 0 and negative quantities without error.
2. **Empty-cart checkout** — the "Place Order" button on the cart page is always enabled, even when the cart is empty.
