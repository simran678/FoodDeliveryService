# High Level Design

## Architecture

```mermaid
flowchart LR
    Client[API Client / curl / Postman]
    Security[Spring Security<br/>HTTP Basic + Role Rules]
    Controllers[Controllers<br/>Admin / Restaurant / Order / Delivery]
    Services[Services<br/>Business Rules + Transactions]
    Repos[Spring Data JPA Repositories]
    DB[(H2 SQL Database)]
    Async[Async NotificationService]
    Notifications[(Notification Table)]

    Client --> Security
    Security --> Controllers
    Controllers --> Services
    Services --> Repos
    Repos --> DB
    Services -. fire and forget .-> Async
    Async --> Notifications
```

## Main Packages

```mermaid
flowchart TB
    root[org.services.fooddeliveryservice]
    controller[controller<br/>HTTP endpoints only]
    service[service<br/>business logic, transactions, ownership checks]
    repository[repository<br/>JPA access, locks]
    domain[domain<br/>entities and enums]
    dto[dto<br/>request and response models]
    exception[exception<br/>structured error handling]
    config[config<br/>security and seed data]

    root --> controller
    root --> service
    root --> repository
    root --> domain
    root --> dto
    root --> exception
    root --> config
```

## Order Placement Flow

```mermaid
sequenceDiagram
    actor Customer
    participant API as OrderController
    participant Security as Spring Security
    participant Service as OrderService
    participant MenuRepo as MenuItemRepository
    participant OrderRepo as FoodOrderRepository
    participant DB as H2 Database
    participant Notify as NotificationService

    Customer->>Security: POST /api/orders
    Security->>API: CUSTOMER authorized
    API->>Service: placeOrder(request, currentUser)
    Service->>DB: begin transaction
    Service->>DB: validate restaurant
    Service->>MenuRepo: lock requested menu items
    MenuRepo->>DB: SELECT menu items FOR UPDATE
    Service->>Service: validate restaurant ownership of menu items
    Service->>Service: validate stock
    Service->>Service: deduct stock
    Service->>OrderRepo: save FoodOrder + OrderItems + Payment
    OrderRepo->>DB: persist order graph
    Service->>DB: commit transaction
    Service-->>Notify: async log notifications
    Service-->>API: OrderResponse
    API-->>Customer: 201 Created
```

## Order Lifecycle

```mermaid
stateDiagram-v2
    [*] --> PLACED
    PLACED --> ACCEPTED: restaurant owner accepts
    PLACED --> REJECTED: restaurant owner rejects
    PLACED --> CANCELLED
    ACCEPTED --> PREPARING: restaurant owner marks preparing
    ACCEPTED --> CANCELLED
    PREPARING --> OUT_FOR_DELIVERY: assigned partner starts delivery
    OUT_FOR_DELIVERY --> DELIVERED: partner delivers
    REJECTED --> [*]
    CANCELLED --> [*]
    DELIVERED --> [*]
```

## Delivery Assignment Flow

```mermaid
sequenceDiagram
    actor Partner
    participant API as DeliveryPartnerOrderController
    participant Service as OrderService
    participant OrderRepo as FoodOrderRepository
    participant AssignmentRepo as DeliveryAssignmentRepository
    participant DB as H2 Database

    Partner->>API: PATCH /api/delivery-partner/orders/{id}/accept
    API->>Service: acceptDelivery(orderId, partnerUser)
    Service->>DB: begin transaction
    Service->>OrderRepo: lock order by id
    OrderRepo->>DB: SELECT order FOR UPDATE
    Service->>Service: require status PREPARING
    Service->>AssignmentRepo: check existing assignment
    Service->>AssignmentRepo: insert assignment
    AssignmentRepo->>DB: unique constraint on order_id
    Service->>DB: commit transaction
    Service-->>API: OrderResponse with deliveryPartnerId
    API-->>Partner: 200 OK
```

## Data Model

```mermaid
erDiagram
    APP_USER ||--o{ RESTAURANT : owns
    CITY ||--o{ RESTAURANT : contains
    RESTAURANT ||--o{ MENU_ITEM : has
    APP_USER ||--o{ FOOD_ORDER : places
    RESTAURANT ||--o{ FOOD_ORDER : receives
    FOOD_ORDER ||--o{ ORDER_ITEM : contains
    MENU_ITEM ||--o{ ORDER_ITEM : referenced_by
    FOOD_ORDER ||--|| PAYMENT : has
    APP_USER ||--|| DELIVERY_PARTNER : profile
    FOOD_ORDER ||--o| DELIVERY_ASSIGNMENT : assigned_once
    DELIVERY_PARTNER ||--o{ DELIVERY_ASSIGNMENT : accepts
    FOOD_ORDER ||--o| RATING_REVIEW : rated_after_delivery
    APP_USER ||--o{ NOTIFICATION : receives

    APP_USER {
        long id
        string username
        string role
    }
    RESTAURANT {
        long id
        string name
    }
    MENU_ITEM {
        long id
        string name
        decimal price
        int stock
        boolean active
    }
    FOOD_ORDER {
        long id
        string status
        decimal totalAmount
    }
    PAYMENT {
        long id
        string status
        decimal amount
    }
    DELIVERY_ASSIGNMENT {
        long id
        instant assignedAt
    }
    RATING_REVIEW {
        long id
        int rating
        string review
    }
```

## Role-Based Access

```mermaid
flowchart LR
    Admin[ADMIN]
    Owner[RESTAURANT_OWNER]
    Customer[CUSTOMER]
    Partner[DELIVERY_PARTNER]

    Admin --> AdminApis[City, restaurant owner, restaurant, delivery partner management]
    Owner --> MenuApis[Menu item management]
    Owner --> OwnerOrderApis[Accept, reject, preparing order transitions]
    Customer --> BrowseApis[Browse restaurants and menus]
    Customer --> OrderApis[Place and track own orders]
    Customer --> RatingApis[Rate delivered own orders]
    Partner --> DeliveryApis[Accept assignment and update assigned orders]
```

## Consistency Rules

- Order placement runs in a service-layer transaction.
- Menu rows are pessimistically locked before stock deduction.
- Menu item stock is validated before deduction and cannot go negative.
- Menu items in an order must belong to the selected restaurant.
- Order status transitions are centralized in `FoodOrder.transitionTo`.
- Delivery assignment locks the order and has a unique database constraint on `order_id`.
- Rating is allowed only when the order is `DELIVERED`.
- Async notification logging runs in a separate transaction and catches failures.
