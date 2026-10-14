package com.fulfilment.application.monolith.fulfillment;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

@QuarkusTest
class WarehouseFulfillmentConstraintTest {

  @Test
  void productCannotUseMoreThanTwoWarehousesForOneStore() {
    long storeId = createStore("CI_PRODUCT_LIMIT_STORE");
    long productId = createProduct("CI_PRODUCT_LIMIT_ITEM");

    assign(storeId, productId, "MWH.001", 201);
    assign(storeId, productId, "MWH.012", 201);
    assign(storeId, productId, "MWH.023", 400);
  }

  @Test
  void storeCannotUseMoreThanThreeDistinctWarehouses() {
    long storeId = createStore("CI_STORE_LIMIT_STORE");
    long first = createProduct("CI_STORE_LIMIT_P1");
    long second = createProduct("CI_STORE_LIMIT_P2");
    long third = createProduct("CI_STORE_LIMIT_P3");
    long fourth = createProduct("CI_STORE_LIMIT_P4");

    assign(storeId, first, "MWH.001", 201);
    assign(storeId, second, "MWH.012", 201);
    assign(storeId, third, "MWH.023", 201);
    assign(storeId, fourth, "MWH.001", 400);
  }

  @Test
  void warehouseCannotStoreMoreThanFiveProductTypes() {
    String warehouseBody = "{\"businessUnitCode\":\"CI_FULFILL_LIMIT_WH\","
        + "\"location\":\"AMSTERDAM-002\","
        + "\"capacity\":10,\"stock\":0}";
    given()
        .contentType("application/json")
        .body(warehouseBody)
        .when()
        .post("/warehouse")
        .then()
        .statusCode(201);

    long storeId = createStore("CI_WAREHOUSE_LIMIT_STORE");
    for (int i = 1; i <= 6; i++) {
      long productId = createProduct("CI_WAREHOUSE_LIMIT_P" + i);
      assign(storeId, productId, "CI_FULFILL_LIMIT_WH", i <= 5 ? 201 : 400);
    }
  }

  private long createStore(String name) {
    Response response = given()
        .contentType("application/json")
        .body("{\"name\":\"" + name + "\",\"quantityProductsInStock\":0}")
        .when()
        .post("/store")
        .then()
        .statusCode(201)
        .extract()
        .response();
    String id = response.jsonPath().getString("id");
    assertNotNull(id);
    return Long.parseLong(id);
  }

  private long createProduct(String name) {
    Response response = given()
        .contentType("application/json")
        .body("{\"name\":\"" + name + "\",\"stock\":0}")
        .when()
        .post("/product")
        .then()
        .statusCode(201)
        .extract()
        .response();
    String id = response.jsonPath().getString("id");
    assertNotNull(id);
    return Long.parseLong(id);
  }

  private void assign(long storeId, long productId, String warehouseCode, int expectedStatus) {
    String request = "{\"storeId\":" + storeId
        + ",\"productId\":" + productId
        + ",\"warehouseBusinessUnitCode\":\"" + warehouseCode + "\"}";
    given()
        .contentType("application/json")
        .body(request)
        .when()
        .post("/fulfillment-assignment")
        .then()
        .statusCode(expectedStatus);
  }
}
