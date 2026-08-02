# ShopMart exploration scratchpad
# Update during Claude Code sessions

## Session
- Date:
- Claude session goal:

## Package map (backend)
- `com.shopmart.entity`:
- `com.shopmart.repository`:
- `com.shopmart.service`:
- `com.shopmart.controller`:
- `com.shopmart.config`:

## Request traces
### POST /api/cart/items
- Controller:
- Service:
- Repository:

### POST /api/orders (place order)
- Controller:
- Service:
- Transaction boundary:

## Bug inventory
| # | Class | Method | Symptom | Severity |
|---|-------|--------|---------|----------|
| 1 | ProductController | list | page 0 skips first page | Medium |
| 2 | CartService | getCart | double loyalty discount | High |
| 3 | OrderService | placeOrder | ghost order if cart clear fails | High |
| 4 | ProductController | getProduct | 500 on bad id | High |
| 5 | OrderService | updateStatus | DELIVERED→PENDING allowed | Medium |
| 6 | CartController | addItem | 200 instead of 201 | Low |

## Sensitive areas (do not refactor casually)
- CartService discount logic
- Order status transitions
- JWT filter / SecurityConfig

## Open questions for next session
-