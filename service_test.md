# Service Test Evidence

Date: 2026-06-21  
Base URL: `http://127.0.0.1:8080`  
Auth: HTTP Basic using seeded users from `DataSeeder`

Seed users:

| Role | Username | Password |
| --- | --- | --- |
| RESTAURANT_OWNER | `owner` | `owner` |
| RESTAURANT_OWNER | `owner2` | `owner2` |
| CUSTOMER | `customer` | `customer` |
| CUSTOMER | `customer2` | `customer2` |
| DELIVERY_PARTNER | `partner` | `partner` |
| DELIVERY_PARTNER | `partner2` | `partner2` |
| DELIVERY_PARTNER | `partner3` | `partner3` |

The service was started with:

```bash
./gradlew bootRun
```

Tests were also run after the delivery-assignment mapping fix:

```bash
./gradlew test
```

Result:

```text
BUILD SUCCESSFUL
```

## 1. Browse Restaurants

Request:

```bash
curl -s -i -u customer:customer http://127.0.0.1:8080/api/restaurants
```

Response:

```http
HTTP/1.1 200
Content-Type: application/json

[{"id":1,"name":"Seed Kitchen","cityId":1,"cityName":"Bengaluru","ownerId":2}]
```

## 2. Browse Menu

Request:

```bash
curl -s -i -u customer:customer http://127.0.0.1:8080/api/restaurants/1/menu-items
```

Response:

```http
HTTP/1.1 200
Content-Type: application/json

[{"id":1,"name":"Paneer Roll","price":149.00,"stock":20,"active":true},{"id":2,"name":"Veg Biryani","price":199.00,"stock":15,"active":true}]
```

## 3. Place Order

Request:

```bash
curl -s -i -u customer:customer \
  -H 'Content-Type: application/json' \
  -d '{"restaurantId":1,"paymentStatus":"SUCCESS","items":[{"menuItemId":1,"quantity":2}]}' \
  http://127.0.0.1:8080/api/orders
```

Response:

```http
HTTP/1.1 201
Content-Type: application/json

{"id":1,"customerId":3,"restaurantId":1,"status":"PLACED","totalAmount":298.00,"paymentStatus":"SUCCESS","deliveryPartnerId":null,"createdAt":"2026-06-21T11:42:47.096519Z","items":[{"menuItemId":1,"name":"Paneer Roll","quantity":2,"unitPrice":149.00}]}
```

## 4. Verify Stock Deduction

Request:

```bash
curl -s -i -u customer:customer http://127.0.0.1:8080/api/restaurants/1/menu-items
```

Response:

```http
HTTP/1.1 200
Content-Type: application/json

[{"id":1,"name":"Paneer Roll","price":149.00,"stock":18,"active":true},{"id":2,"name":"Veg Biryani","price":199.00,"stock":15,"active":true}]
```

Stock for `Paneer Roll` moved from `20` to `18`.

## 5. Restaurant Owner Accepts Order

Request:

```bash
curl -s -i -u owner:owner -X PATCH http://127.0.0.1:8080/api/restaurant-owner/orders/1/accept
```

Response:

```http
HTTP/1.1 200
Content-Type: application/json

{"id":1,"customerId":3,"restaurantId":1,"status":"ACCEPTED","totalAmount":298.00,"paymentStatus":"SUCCESS","deliveryPartnerId":null,"createdAt":"2026-06-21T11:42:47.096519Z","items":[{"menuItemId":1,"name":"Paneer Roll","quantity":2,"unitPrice":149.00}]}
```

## 6. Restaurant Owner Marks Preparing

Request:

```bash
curl -s -i -u owner:owner -X PATCH http://127.0.0.1:8080/api/restaurant-owner/orders/1/preparing
```

Response:

```http
HTTP/1.1 200
Content-Type: application/json

{"id":1,"customerId":3,"restaurantId":1,"status":"PREPARING","totalAmount":298.00,"paymentStatus":"SUCCESS","deliveryPartnerId":null,"createdAt":"2026-06-21T11:42:47.096519Z","items":[{"menuItemId":1,"name":"Paneer Roll","quantity":2,"unitPrice":149.00}]}
```

## 7. Delivery Partner Accepts Assignment

Request:

```bash
curl -s -i -u partner:partner -X PATCH http://127.0.0.1:8080/api/delivery-partner/orders/1/accept
```

Response:

```http
HTTP/1.1 200
Content-Type: application/json

{"id":1,"customerId":3,"restaurantId":1,"status":"PREPARING","totalAmount":298.00,"paymentStatus":"SUCCESS","deliveryPartnerId":1,"createdAt":"2026-06-21T11:42:47.096519Z","items":[{"menuItemId":1,"name":"Paneer Roll","quantity":2,"unitPrice":149.00}]}
```

## 8. Rating Before Delivery Is Rejected

Request:

```bash
curl -s -i -u customer:customer \
  -H 'Content-Type: application/json' \
  -d '{"rating":5,"review":"Too early"}' \
  http://127.0.0.1:8080/api/orders/1/ratings
```

Response:

```http
HTTP/1.1 400
Content-Type: application/json

{"status":400,"message":"Rating is allowed only after delivery","errorCode":"RATING_NOT_ALLOWED"}
```

## 9. Delivery Partner Marks Out For Delivery

Request:

```bash
curl -s -i -u partner:partner -X PATCH http://127.0.0.1:8080/api/delivery-partner/orders/1/out-for-delivery
```

Response:

```http
HTTP/1.1 200
Content-Type: application/json

{"id":1,"customerId":3,"restaurantId":1,"status":"OUT_FOR_DELIVERY","totalAmount":298.00,"paymentStatus":"SUCCESS","deliveryPartnerId":1,"createdAt":"2026-06-21T11:42:47.096519Z","items":[{"menuItemId":1,"name":"Paneer Roll","quantity":2,"unitPrice":149.00}]}
```

## 10. Delivery Partner Marks Delivered

Request:

```bash
curl -s -i -u partner:partner -X PATCH http://127.0.0.1:8080/api/delivery-partner/orders/1/delivered
```

Response:

```http
HTTP/1.1 200
Content-Type: application/json

{"id":1,"customerId":3,"restaurantId":1,"status":"DELIVERED","totalAmount":298.00,"paymentStatus":"SUCCESS","deliveryPartnerId":1,"createdAt":"2026-06-21T11:42:47.096519Z","items":[{"menuItemId":1,"name":"Paneer Roll","quantity":2,"unitPrice":149.00}]}
```

## 11. Rating After Delivery Succeeds

Request:

```bash
curl -s -i -u customer:customer \
  -H 'Content-Type: application/json' \
  -d '{"rating":5,"review":"Delivered hot and on time"}' \
  http://127.0.0.1:8080/api/orders/1/ratings
```

Response:

```http
HTTP/1.1 201
Content-Type: application/json

{"id":1}
```

## 12. Insufficient Stock Is Rejected

Request:

```bash
curl -s -i -u customer:customer \
  -H 'Content-Type: application/json' \
  -d '{"restaurantId":1,"paymentStatus":"SUCCESS","items":[{"menuItemId":2,"quantity":999}]}' \
  http://127.0.0.1:8080/api/orders
```

Response:

```http
HTTP/1.1 400
Content-Type: application/json

{"status":400,"message":"Insufficient stock","errorCode":"INSUFFICIENT_STOCK"}
```

## 13. Invalid Transition Is Rejected

Request:

```bash
curl -s -i -u owner:owner -X PATCH http://127.0.0.1:8080/api/restaurant-owner/orders/1/preparing
```

Response:

```http
HTTP/1.1 400
Content-Type: application/json

{"status":400,"message":"Invalid order status transition","errorCode":"INVALID_STATE"}
```

## 14. Unauthorized Admin Access Is Rejected

Request:

```bash
curl -s -i -u customer:customer \
  -H 'Content-Type: application/json' \
  -d '{"name":"Forbidden City"}' \
  http://127.0.0.1:8080/api/admin/cities
```

Response:

```http
HTTP/1.1 403
Content-Type: application/json

{"timestamp":"2026-06-21T11:42:49.938Z","status":403,"error":"Forbidden","path":"/api/admin/cities"}
```

## 15. Delivery Assignment Contention

Create second order:

```bash
curl -s -i -u customer:customer \
  -H 'Content-Type: application/json' \
  -d '{"restaurantId":1,"paymentStatus":"SUCCESS","items":[{"menuItemId":1,"quantity":1}]}' \
  http://127.0.0.1:8080/api/orders
```

Response:

```http
HTTP/1.1 201
Content-Type: application/json

{"id":2,"customerId":3,"restaurantId":1,"status":"PLACED","totalAmount":149.00,"paymentStatus":"SUCCESS","deliveryPartnerId":null,"createdAt":"2026-06-21T11:42:50.236394Z","items":[{"menuItemId":1,"name":"Paneer Roll","quantity":1,"unitPrice":149.00}]}
```

Move second order to `PREPARING`:

```bash
curl -s -i -u owner:owner -X PATCH http://127.0.0.1:8080/api/restaurant-owner/orders/2/accept
curl -s -i -u owner:owner -X PATCH http://127.0.0.1:8080/api/restaurant-owner/orders/2/preparing
```

First delivery assignment:

```bash
curl -s -i -u partner:partner -X PATCH http://127.0.0.1:8080/api/delivery-partner/orders/2/accept
```

Response:

```http
HTTP/1.1 200
Content-Type: application/json

{"id":2,"customerId":3,"restaurantId":1,"status":"PREPARING","totalAmount":149.00,"paymentStatus":"SUCCESS","deliveryPartnerId":1,"createdAt":"2026-06-21T11:42:50.236394Z","items":[{"menuItemId":1,"name":"Paneer Roll","quantity":1,"unitPrice":149.00}]}
```

Duplicate assignment attempt:

```bash
curl -s -i -u partner:partner -X PATCH http://127.0.0.1:8080/api/delivery-partner/orders/2/accept
```

Response:

```http
HTTP/1.1 409
Content-Type: application/json

{"status":409,"message":"Order already assigned","errorCode":"ORDER_ALREADY_ASSIGNED"}
```

## Concurrent Flow Seed Data

The seed data includes multiple restaurants and delivery partners so contention scenarios can be tested manually:

- Restaurant `1`: `Seed Kitchen`, owned by `owner`
- Restaurant `2`: `Coastal Cart`, owned by `owner2`
- Delivery partners: `partner`, `partner2`, `partner3`
- Low-stock item: `Low Stock Thali`, stock `1`

Example curls for competing delivery assignment:

```bash
# Create an order.
curl -s -i -u customer:customer \
  -H 'Content-Type: application/json' \
  -d '{"restaurantId":1,"paymentStatus":"SUCCESS","items":[{"menuItemId":1,"quantity":1}]}' \
  http://127.0.0.1:8080/api/orders

# Move it to PREPARING.
curl -s -i -u owner:owner -X PATCH http://127.0.0.1:8080/api/restaurant-owner/orders/{orderId}/accept
curl -s -i -u owner:owner -X PATCH http://127.0.0.1:8080/api/restaurant-owner/orders/{orderId}/preparing

# In separate terminals, try to accept the same order with different partners.
curl -s -i -u partner:partner -X PATCH http://127.0.0.1:8080/api/delivery-partner/orders/{orderId}/accept
curl -s -i -u partner2:partner2 -X PATCH http://127.0.0.1:8080/api/delivery-partner/orders/{orderId}/accept
curl -s -i -u partner3:partner3 -X PATCH http://127.0.0.1:8080/api/delivery-partner/orders/{orderId}/accept
```

Expected result: exactly one partner receives `200 OK`; the others receive `409 ORDER_ALREADY_ASSIGNED`.

Example curls for low-stock contention:

```bash
# Low Stock Thali has stock 1 in the default seed data.
curl -s -i -u customer:customer \
  -H 'Content-Type: application/json' \
  -d '{"restaurantId":1,"paymentStatus":"SUCCESS","items":[{"menuItemId":3,"quantity":1}]}' \
  http://127.0.0.1:8080/api/orders

curl -s -i -u customer2:customer2 \
  -H 'Content-Type: application/json' \
  -d '{"restaurantId":1,"paymentStatus":"SUCCESS","items":[{"menuItemId":3,"quantity":1}]}' \
  http://127.0.0.1:8080/api/orders
```

Expected result: one order succeeds and the other receives `400 INSUFFICIENT_STOCK`.

## Notes From Live Verification

The live curl test exposed an incorrect initial JPA mapping: `DeliveryAssignment.deliveryPartner` was modeled as `@OneToOne`, which allowed only one lifetime assignment per delivery partner. The requirement is one delivery partner per order, while a delivery partner can handle multiple orders over time. This was corrected to `@ManyToOne`, and `./gradlew test` passed after the fix.
