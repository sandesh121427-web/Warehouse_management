package com.fulfilment.application.monolith.products;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ProductEndpointTest {

  @Test
  void productCrudDoesNotMutateSeedData() {
    given()
        .when()
        .get("/product")
        .then()
        .statusCode(200)
        .body(containsString("TONSTAD"), containsString("KALLAX"), containsString("BESTÅ"));

    String request = "{\"name\":\"CI_PRODUCT_CRUD_TEST\","
        + "\"description\":\"Created by integration test\","
        + "\"price\":12.50,\"stock\":2}";

    Response created = given()
        .contentType("application/json")
        .body(request)
        .when()
        .post("/product")
        .then()
        .statusCode(201)
        .extract()
        .response();

    String id = created.jsonPath().getString("id");
    assertNotNull(id);

    given().when().delete("/product/" + id).then().statusCode(204);

    given()
        .when()
        .get("/product")
        .then()
        .statusCode(200)
        .body(not(containsString("CI_PRODUCT_CRUD_TEST")),
            containsString("TONSTAD"), containsString("KALLAX"), containsString("BESTÅ"));
  }
}
