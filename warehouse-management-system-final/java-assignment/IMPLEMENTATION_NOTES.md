# Implementation notes

## Warehouse API

The warehouse HTTP adapter is implemented directly with JAX-RS under
`com.fulfilment.application.monolith.warehouses.adapters.restapi`. This avoids requiring IDEs to
index generated Java interfaces before the project can compile. The OpenAPI YAML remains in
`src/main/resources/openapi/warehouse-openapi.yaml` as the endpoint contract reference.

Warehouse creation validates the business-unit code, location, per-location warehouse count,
location capacity, aggregate capacity and stock/capacity relationship. Replacement runs in one
transaction: it validates the replacement, archives the current active row, and creates a new row
with a new database ID while preserving the business-unit code and stock quantity. Archived rows
are retained in the database.

## Store synchronization

Store create/update/patch registers a JTA interposed synchronization. The legacy gateway is called
only from `afterCompletion` when the transaction status is `STATUS_COMMITTED`. A snapshot is passed
to the gateway so it does not depend on a managed entity remaining attached after commit.

## Fulfillment limits

The fulfillment assignment endpoint enforces at most two warehouses for a product at a store, at
most three distinct warehouses for a store, and at most five distinct product types per warehouse.
Repeated assignment of the same store/product/warehouse tuple is idempotent.

## Testing

The test suite contains domain unit tests and Quarkus REST integration tests. GitHub Actions runs
`./mvnw --batch-mode --no-transfer-progress verify` on Java 17. The build must be run in an environment
with Maven Central access so Maven can download the wrapper distribution and dependencies.
