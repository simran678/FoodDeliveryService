# AGENTS.md

## AI Usage Disclosure

This project was built as part of a machine coding assignment for a Food Delivery Order Management backend system.

AI assistance was used during development as an engineering support tool for requirement analysis, feature scoping, API design, database modeling, edge-case identification, test planning, documentation structuring, and code review suggestions.

The final implementation, design decisions, trade-offs, testing, debugging, and submission were reviewed and owned by the developer.

---

## Assignment Context

The assignment is to build a Food Delivery Order Management system using Spring Boot.

The product requirement is intentionally open-ended, so the implementation choices, assumptions, scoping decisions, APIs, entities, edge cases, and trade-offs are part of the evaluation.

The system supports multiple restaurants across cities, menu management per restaurant, customer order placement, order lifecycle management, delivery partner assignment, stock-safe ordering, asynchronous status notifications, ratings, and reviews.

---

## Project Scope

The application supports the following core flows:

* Admin management of cities, restaurants, and delivery partners
* Restaurant owner menu management
* Restaurant owner order acceptance and rejection
* Customer restaurant browsing
* Customer menu browsing
* Customer order placement
* Customer order tracking
* Delivery partner assignment
* Delivery partner order status updates
* Order lifecycle transitions
* Stock deduction during order placement
* Payment status persistence
* Asynchronous notification handling
* Ratings and reviews after delivery
* Basic role-based access control
* Input validation and structured error handling
* Unit and integration tests for core flows

---

## Technology Stack

The project uses:

* Java
* Spring Boot
* Spring Web
* Spring Data JPA
* Spring Security
* H2 Database
* SQL database persistence
* JUnit
* Mockito

H2 has been used as the SQL database for this machine coding project because it is lightweight, easy to run locally, and suitable for quick evaluation without requiring external database setup.

The use of H2 keeps the project self-contained while still demonstrating SQL persistence, relational modeling, JPA mappings, transactions, constraints, and repository-based data access.

---

## Roles Supported

The system supports the following roles:

```text id="3pz6ad"
ADMIN
RESTAURANT_OWNER
CUSTOMER
DELIVERY_PARTNER
```

### Admin

Admin can manage:

* Cities
* Restaurants
* Restaurant owners
* Delivery partners

### Restaurant Owner

Restaurant owner can:

* Manage restaurant menu
* Add menu items
* Update menu item details
* Update menu item stock
* Accept customer orders
* Reject customer orders
* Mark accepted orders as preparing

### Customer

Customer can:

* Browse restaurants
* Browse menu items
* Place orders
* Track order status
* Rate delivered orders
* Review delivered orders

### Delivery Partner

Delivery partner can:

* View assignable or assigned orders
* Accept delivery assignment
* Mark order as out for delivery
* Mark order as delivered

---

## Skills Used During Development

The following skills were used while building this project:

* Requirement analysis
* Feature scoping
* REST API design
* SQL database modeling
* Entity relationship design
* Spring Boot development
* Spring Data JPA usage
* Transaction management
* Role-based access control
* Input validation
* Global exception handling
* Order lifecycle modeling
* State transition validation
* Concurrency handling
* Stock consistency handling
* Delivery assignment handling
* Asynchronous notification design
* Unit testing
* Integration testing
* Documentation
* AI-assisted engineering workflow
* Code review and refactoring

---

## Development Guidelines Followed

### 1. Layered Architecture

The application follows a clean layered structure:

```text id="icnuvv"
Controller -> Service -> Repository -> Database
```

Responsibilities are separated as follows:

* Controllers handle HTTP requests and responses.
* Services contain business logic.
* Repositories handle database access.
* DTOs are used for API request and response models.
* Entities are used only for persistence.
* Exceptions are handled through a global exception handler.

Business logic is intentionally kept out of controllers.

---

### 2. REST API Design

APIs are designed around resources and user roles.

The main API areas are:

* Admin APIs
* Restaurant owner APIs
* Customer APIs
* Delivery partner APIs
* Order APIs
* Rating and review APIs

The APIs use proper HTTP methods:

* `GET` for fetching data
* `POST` for creating resources
* `PATCH` for partial updates and status transitions
* `DELETE` only where deletion is explicitly needed

Example API areas:

```text id="iz9mzy"
POST   /api/admin/cities
POST   /api/admin/restaurants
POST   /api/admin/delivery-partners

POST   /api/restaurants/{restaurantId}/menu-items
PATCH  /api/restaurants/{restaurantId}/menu-items/{menuItemId}

GET    /api/restaurants
GET    /api/restaurants/{restaurantId}/menu-items
POST   /api/orders
GET    /api/orders/{orderId}

PATCH  /api/restaurant-owner/orders/{orderId}/accept
PATCH  /api/restaurant-owner/orders/{orderId}/reject
PATCH  /api/restaurant-owner/orders/{orderId}/preparing

PATCH  /api/delivery-partner/orders/{orderId}/accept
PATCH  /api/delivery-partner/orders/{orderId}/out-for-delivery
PATCH  /api/delivery-partner/orders/{orderId}/delivered

POST   /api/orders/{orderId}/ratings
```

---

### 3. SQL Database Design

The system uses H2 as the relational SQL database for local development, testing, and assignment evaluation.

The core entities include:

* User
* City
* Restaurant
* MenuItem
* Order
* OrderItem
* Payment
* DeliveryPartner
* DeliveryAssignment
* RatingReview
* Notification

Important database design rules:

* A city can have multiple restaurants.
* A restaurant can have multiple menu items.
* A restaurant owner can manage assigned restaurant data.
* A customer can place multiple orders.
* An order belongs to one customer.
* An order belongs to one restaurant.
* An order contains multiple order items.
* Order items reference menu items.
* Payment details are associated with an order.
* Delivery assignment is associated with an order and a delivery partner.
* Rating and review are allowed only after delivery.
* Menu item stock should not become negative.
* Each order should follow a controlled lifecycle.

---

### 4. Order Placement

Order placement is one of the most important flows in the system.

During order placement, the system should:

1. Validate the customer.
2. Validate the restaurant.
3. Validate all requested menu items.
4. Ensure all menu items belong to the selected restaurant.
5. Validate stock availability.
6. Deduct item stock.
7. Create the order.
8. Create order item records.
9. Persist payment details.
10. Persist the initial order status.
11. Trigger asynchronous notifications after the main operation.

Order placement should atomically reflect:

* Item stock
* Order state
* Payment state

If any critical step fails, the transaction should roll back.

---

### 5. Transaction Management

Transactional consistency is required for order placement.

The order placement flow should be handled inside a service-layer transaction.

Critical transactional operations include:

* Stock validation
* Stock deduction
* Order creation
* Order item creation
* Payment record creation
* Initial order state persistence

External side effects such as notifications should not break the main order placement transaction.

---

### 6. Concurrency Handling

The system should prevent race conditions in critical flows.

Important concurrency-sensitive cases:

* Multiple customers ordering the same low-stock menu item
* Multiple delivery partners trying to accept the same order
* Conflicting order status updates

The implementation should use transaction-safe database operations, locking, optimistic locking, or conditional updates where required.

The goal is to ensure:

* Stock is not oversold.
* One order is assigned to only one delivery partner.
* Invalid concurrent status changes are rejected.

---

### 7. Order Status Lifecycle

The order lifecycle is controlled through valid state transitions.

Common order states:

```text id="uebbgt"
PLACED
ACCEPTED
REJECTED
PREPARING
OUT_FOR_DELIVERY
DELIVERED
CANCELLED
```

Expected lifecycle:

```text id="gap7dp"
PLACED -> ACCEPTED -> PREPARING -> OUT_FOR_DELIVERY -> DELIVERED
```

Other supported transitions may include:

```text id="vy2f73"
PLACED -> REJECTED
PLACED -> CANCELLED
ACCEPTED -> CANCELLED
```

Invalid transitions should be rejected.

Examples:

* A delivered order should not move back to preparing.
* A rejected order should not be accepted later.
* An out-for-delivery order should not be moved back to accepted.
* A rating should be allowed only after the order is delivered.

---

### 8. Delivery Partner Assignment

Delivery partner assignment should handle contention.

Important rules:

* An order can be assigned to only one delivery partner.
* If multiple partners try to accept the same order, only one should succeed.
* Once assigned, the delivery partner can update the delivery status.
* Other delivery partners should not be allowed to update the same order.

This flow should be implemented safely using database-level consistency.

---

### 9. Payment Handling

The assignment requires order placement to atomically reflect payment state.

This project models payment status internally.

Payment states may include:

```text id="yhtdeb"
PENDING
SUCCESS
FAILED
REFUNDED
```

For machine coding scope, real payment gateway integration is intentionally not implemented.

The system persists payment details as part of the order placement flow.

---

### 10. Role-Based Access Control

The system supports basic role-based access control.

Access rules:

* Admin can manage cities, restaurants, and delivery partners.
* Restaurant owner can manage menu and accept/reject orders.
* Customer can browse, order, track, rate, and review.
* Delivery partner can accept assignments and update delivery status.

Authorization checks should be applied at the API level and, where required, at the service level.

Ownership rules should also be enforced.

Examples:

* A customer should not access another customer's order.
* A restaurant owner should not modify another restaurant's menu.
* A delivery partner should not update an order assigned to someone else.

---

### 11. Validation and Error Handling

All request payloads should be validated using DTO-level validation.

The application should return structured errors for cases such as:

* Invalid request body
* City not found
* Restaurant not found
* Menu item not found
* Insufficient stock
* Invalid order status transition
* Unauthorized access
* Order already assigned
* Payment failure
* Rating not allowed before delivery

Error responses should be clear and predictable.

Example error response:

```json id="tjaoew"
{
  "status": 400,
  "message": "Invalid order status transition",
  "errorCode": "INVALID_ORDER_STATUS"
}
```

---

### 12. Asynchronous Notifications

Status updates should fan out asynchronously to relevant users.

Notifications may be sent to:

* Customer
* Restaurant owner
* Delivery partner

Notification delivery should not block the calling API flow.

If notification delivery fails, the main order operation should not fail.

The notification implementation can be simple for machine coding scope, such as:

* Application event
* Async service
* In-memory notification logging

---

### 13. Ratings and Reviews

Ratings and reviews are supported after delivery.

Important rules:

* Only the customer who placed the order can rate or review it.
* Rating should be allowed only after the order reaches `DELIVERED`.
* A customer should not be allowed to rate an undelivered order.
* Duplicate rating behavior should be explicitly handled.

---

### 14. Testing Guidelines

The project should include unit and integration tests for core flows.

Important unit test cases:

* Successful order placement
* Order placement failure due to insufficient stock
* Stock deduction after order placement
* Invalid order status transition
* Restaurant owner accepting an order
* Restaurant owner rejecting an order
* Delivery partner accepting an order
* Rating allowed only after delivery
* Rating rejected before delivery

Important integration test cases:

* Order placement API persists order and order items
* H2 database persistence works correctly
* Stock is deducted correctly
* Unauthorized role access is blocked
* Only one delivery partner can accept an order
* Invalid status transition returns an error response

---

## AI Assistance Scope

AI was used for:

* Understanding assignment requirements
* Interpreting the open-ended product scope
* Breaking the problem into entities and APIs
* Designing the order lifecycle
* Identifying edge cases
* Planning unit and integration tests
* Reviewing code structure
* Improving documentation
* Suggesting cleaner naming and organization
* Preparing this AGENTS.md file

AI was not used to blindly generate the final submission.

All code, tests, design decisions, assumptions, and trade-offs were reviewed manually.

---

## Raw Files Used During Development

The assignment requires raw files used during development to be included.

The repository should include relevant raw planning or AI-assistance files where applicable, such as:

* Assignment PDF
* Prompt notes
* Requirement breakdown notes
* API planning notes
* Entity planning notes
* Test planning notes

These files are included only for transparency around the development process.

---

## README Documentation Expectations

The README should document:

* Problem interpretation
* Features implemented
* Assumptions made
* Tech stack used
* Reason for using H2 database
* How to run the application
* How to run tests
* API examples
* Database design overview
* Order lifecycle
* Role-based access control approach
* Concurrency handling approach
* Testing approach
* AI workflow used
* Known limitations

---

## Video Explanation Expectations

The final video explanation should cover:

* How the problem was approached
* High-level solution design
* Tech stack used and reasoning
* Why H2 database was used
* Core APIs implemented
* Database design
* Order placement flow
* Order lifecycle flow
* Delivery assignment flow
* Testing approach
* AI workflow used during development

---

## Out of Scope

The following were intentionally kept out of scope:

* Frontend UI
* Deployment
* Containerization
* CI/CD
* Distributed systems
* Microservices
* OAuth
* SSO
* MFA
* Production-grade observability
* Monitoring
* Alerting
* Real payment gateway integration
* Real SMS/email/push provider integration

The focus is on machine-coding expectations: clean backend design, working REST APIs, H2-backed SQL persistence, validation, transaction management, role-based access control, business correctness, and tests.

---

## Final Submission Checklist

Before submitting, verify:

* Application starts successfully.
* REST APIs are implemented.
* H2 database configuration is present.
* SQL database persistence works.
* Admin APIs work.
* Restaurant owner APIs work.
* Customer APIs work.
* Delivery partner APIs work.
* Order placement is transactional.
* Stock is handled safely.
* Payment status is persisted.
* Role-based access control is present.
* Invalid order transitions are rejected.
* Delivery assignment handles contention.
* Ratings are allowed only for delivered orders.
* Validation errors are handled cleanly.
* Unit tests are present.
* Integration tests are present.
* Tests pass.
* README is complete.
* `AGENTS.md` or `CLAUDE.md` is present at the root of the repository.
* Skills used during development are documented.
* Raw files used during development are included where applicable.
* GitHub repository has multiple meaningful commits.
