package com.fulfilment.application.monolith.fulfillment;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
class WarehouseFulfillmentEndpointTest {

  @Test
  void assignsAnActiveWarehouseToAStoreAndProduct() {
    String request = "{\"storeId\":1,\"productId\":1,"
        + "\"warehouseBusinessUnitCode\":\"MWH.001\"}";

    given()
        .contentType("application/json")
        .body(request)
        .when()
        .post("/fulfillment-assignment")
        .then()
        .statusCode(anyOf(is(200), is(201)));
  }
}
