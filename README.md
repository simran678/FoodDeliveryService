# Food Delivery Order Management Backend

Spring Boot backend for a machine-coding style food delivery order management system. The project follows a layered architecture:

```text
Controller -> Service -> Repository -> H2 Database
```

## Tech Stack

- Java 21
- Spring Boot 4.1
- Spring Web MVC
- Spring Data JPA
- Spring Security with HTTP Basic auth
- Bean Validation
- H2 in-memory SQL database
- JUnit, Mockito, Spring Boot test support

## Scope Implemented

- Admin management for cities, restaurant owners, restaurants, and delivery partners
- Restaurant owner menu creation and updates
- Customer restaurant and menu browsing
- Transactional customer order placement
- Stock validation and stock deduction
- Payment status persistence on order placement
- Restaurant owner accept, reject, and preparing transitions
- Delivery partner assignment and delivery status transitions
- Ratings and reviews only after delivery
- Role-based API access and service-level ownership checks
- Structured validation and error responses
- Asynchronous notification logging that does not fail the main transaction
- Seed data for local usage

## Local Seed Users

The application seeds these users outside the `test` profile:

| Role | Username | Password |
| --- | --- | --- |
| ADMIN | `admin` | `admin` |
| RESTAURANT_OWNER | `owner` | `owner` |
| RESTAURANT_OWNER | `owner2` | `owner2` |
| CUSTOMER | `customer` | `customer` |
| CUSTOMER | `customer2` | `customer2` |
| DELIVERY_PARTNER | `partner` | `partner` |
| DELIVERY_PARTNER | `partner2` | `partner2` |
| DELIVERY_PARTNER | `partner3` | `partner3` |

Use HTTP Basic auth for API calls.

The seed data also creates:

- City: `Bengaluru`
- City: `Mumbai`
- Restaurant: `Seed Kitchen`, owned by `owner`
- Restaurant: `Coastal Cart`, owned by `owner2`
- `Seed Kitchen` menu items:
  - `Paneer Roll`, price `149.00`, stock `20`
  - `Veg Biryani`, price `199.00`, stock `15`
  - `Low Stock Thali`, price `249.00`, stock `1`
- `Coastal Cart` menu items:
  - `Fish Curry Rice`, price `299.00`, stock `10`
  - `Sol Kadhi`, price `99.00`, stock `25`

Reasoning: the seed data keeps manual evaluation fast. A reviewer can start the service and immediately exercise browsing, order placement, owner transitions, delivery assignment, rating flows, cross-restaurant ownership checks, low-stock ordering, and competing delivery partner assignment attempts without first creating setup data through admin APIs.

## Run

```bash
./gradlew bootRun
```

H2 console:

```text
http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:fooddelivery
User: sa
Password:
```

## Test

```bash
./gradlew test
```

Tests cover successful order placement, insufficient stock, stock deduction, invalid transitions, owner accept/reject flows, delivery assignment contention, rating before/after delivery, and unauthorized access.

## Key APIs

Admin:

```text
POST /api/admin/cities
POST /api/admin/restaurant-owners
POST /api/admin/restaurants
POST /api/admin/delivery-partners
GET  /api/admin/delivery-partners/available
PATCH /api/admin/orders/{orderId}/delivery-partners/{partnerId}/assign
```

Restaurant and menu:

```text
GET   /api/restaurants
GET   /api/restaurants?cityId={cityId}
GET   /api/restaurants/{restaurantId}/menu-items
POST  /api/restaurants/{restaurantId}/menu-items
PATCH /api/restaurants/{restaurantId}/menu-items/{menuItemId}
```

Orders:

```text
POST /api/orders
GET  /api/orders/{orderId}

PATCH /api/restaurant-owner/orders/{orderId}/accept
PATCH /api/restaurant-owner/orders/{orderId}/reject
PATCH /api/restaurant-owner/orders/{orderId}/preparing

PATCH /api/delivery-partner/orders/{orderId}/accept
PATCH /api/delivery-partner/orders/{orderId}/out-for-delivery
PATCH /api/delivery-partner/orders/{orderId}/delivered

POST /api/orders/{orderId}/ratings
```

Example order request:

```json
{
  "restaurantId": 1,
  "paymentStatus": "SUCCESS",
  "items": [
    {
      "menuItemId": 1,
      "quantity": 2
    }
  ]
}
```

## Order Lifecycle

Primary lifecycle:

```text
PLACED -> ACCEPTED -> PREPARING -> OUT_FOR_DELIVERY -> DELIVERED
```

Additional supported transitions:

```text
PLACED -> REJECTED
PLACED -> CANCELLED
ACCEPTED -> CANCELLED
```

Invalid transitions throw a structured `400` response.

## Concurrency and Consistency

- Order placement is wrapped in a service-layer transaction.
- Menu items are loaded with a pessimistic write lock during order placement.
- Stock is validated and deducted inside the same transaction as order and payment persistence.
- Menu item stock cannot be deducted below zero.
- Delivery assignment uses both a pessimistic order lock and a unique database constraint on `order_id`.
- Order status updates lock the order before applying lifecycle transitions.
- Notification logging runs asynchronously in a separate transaction and catches failures.

## Authorization and Ownership

- URL-level role access is enforced by Spring Security.
- Service methods also enforce ownership rules:
  - Restaurant owners can modify only their own restaurant/menu/orders.
  - Customers can track and rate only their own orders.
  - Delivery partners can update only orders assigned to them.

## Error Response

Errors are returned in a predictable shape:

```json
{
  "status": 400,
  "message": "Invalid order status transition",
  "errorCode": "INVALID_STATE"
}
```

## Assumptions and Reasoning

- This is intentionally a backend-only implementation. The assignment is evaluated on API design, persistence, transactions, access control, business rules, and tests, so frontend, deployment, and production operations are left out.

- H2 is used as an in-memory SQL database to keep the project easy to run locally. It still demonstrates relational modeling, JPA mappings, transactions, locks, and constraints without requiring an external database.

- Seed data is included for manual review, but disabled in the `test` profile. The seeded users, restaurants, menu items, low-stock item, and delivery partners make curl testing quick; tests create their own data so they remain deterministic.

- Seed credentials are simple on purpose. Users like `admin/admin`, `owner/owner`, `customer/customer`, and `partner/partner` are for local evaluation only; passwords are still encoded before being stored.

- HTTP Basic auth is used because it is enough to demonstrate role-based access. OAuth, JWT, SSO, MFA, and account recovery would add identity complexity without improving the core order-management evaluation.

- Authorization is enforced in two places. Spring Security blocks role-level access at the API boundary, while services enforce ownership rules such as owners managing only their restaurants, customers seeing only their orders, and partners updating only assigned orders.

- Users are admin-managed rather than self-registered. This keeps the role setup explicit and focused on the assignment flows.

- Each restaurant has one owner, each menu item belongs to one restaurant, and each order belongs to one restaurant. This makes ownership, menu validation, and restaurant order handling clear.

- Stock is modeled as a simple integer count. That is enough to prove the important rule: stock is checked and deducted transactionally and must not go negative.

- Payment is represented as an internal status, not a real gateway integration. The client supplies the payment status so the system can persist payment state as part of order placement without external dependencies.

- Order placement is transactional because stock, order items, order total, and payment state must succeed or roll back together. Menu rows are pessimistically locked to prevent overselling under concurrent orders.

- Order state changes are intentionally limited to the documented lifecycle. The transition rules live in `FoodOrder.transitionTo` so invalid moves, such as `DELIVERED -> PREPARING`, are rejected consistently.

- Delivery partners manually accept orders in `PREPARING`. This keeps dispatch simple while still testing the important contention rule: an order can be assigned only once, enforced with locking and a unique constraint on `order_id`.

- A delivery partner can take multiple orders over time, but only the assigned partner can update a given order's delivery status.

- Ratings and reviews are allowed only after delivery, and only once per order. This keeps review behavior explicit and avoids duplicate ratings.

- Notifications are asynchronous database logs. They demonstrate non-blocking side effects without introducing SMS, email, or push-provider setup, and failures do not roll back the main operation.

- DTOs are used for API contracts and validation instead of exposing JPA entities directly. Errors are returned in a consistent structured shape for predictable client behavior.

- Java 21 and Spring Boot 4.1 are used because they match the generated project and keep the implementation current without adding extra framework complexity.

## Known Limitations

- No frontend UI is included.
- No production database configuration is included.
- No real payment gateway is integrated.
- No real SMS, email, or push provider is integrated.
- No OAuth, SSO, MFA, refresh tokens, or user self-service account flows are included.
- No route optimization, distance calculation, delivery pricing, or partner availability scheduling is implemented.
- No CI/CD, Docker setup, deployment scripts, monitoring, alerting, or production observability is included.
