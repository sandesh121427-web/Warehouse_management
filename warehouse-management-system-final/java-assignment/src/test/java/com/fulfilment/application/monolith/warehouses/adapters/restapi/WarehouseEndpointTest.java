package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;

import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

@QuarkusTest
class WarehouseEndpointTest {

  @Inject WarehouseRepository warehouseRepository;

  @Test
  void listsSeededActiveWarehouses() {
    given()
        .when()
        .get("/warehouse")
        .then()
        .statusCode(200)
        .body(containsString("MWH.001"), containsString("MWH.012"), containsString("MWH.023"));
  }

  @Test
  void createsReadsAndArchivesWarehouse() {
    String request = "{\"businessUnitCode\":\"CI.TEST.001\","
        + "\"location\":\"AMSTERDAM-002\",\"capacity\":20,\"stock\":10}";

    Response response = given()
        .contentType("application/json")
        .body(request)
        .when()
        .post("/warehouse")
        .then()
        .statusCode(201)
        .extract()
        .response();

    String id = response.jsonPath().getString("id");
    org.junit.jupiter.api.Assertions.assertNotNull(id);

    given().when().get("/warehouse/" + id).then()
        .statusCode(200)
        .body(containsString("CI.TEST.001"));

    given().when().delete("/warehouse/" + id).then().statusCode(204);

    given().when().get("/warehouse").then()
        .statusCode(200)
        .body(not(containsString("CI.TEST.001")));
  }


  @Test
  void replacesWarehouseAndKeepsBusinessUnitCode() {
    String request = "{\"location\":\"AMSTERDAM-001\","
        + "\"capacity\":60,\"stock\":5}";

    given()
        .contentType("application/json")
        .body(request)
        .when()
        .post("/warehouse/MWH.012/replacement")
        .then()
        .statusCode(200)
        .body(containsString("MWH.012"), containsString("AMSTERDAM-001"));

    java.util.List<DbWarehouse> versions = warehouseRepository
        .find("businessUnitCode = ?1", "MWH.012")
        .list();
    org.junit.jupiter.api.Assertions.assertEquals(2, versions.size());
    org.junit.jupiter.api.Assertions.assertEquals(1,
        versions.stream().filter(version -> version.archivedAt != null).count());
    org.junit.jupiter.api.Assertions.assertEquals(1,
        versions.stream().filter(version -> version.archivedAt == null).count());
  }

  @Test
  void rejectsWarehouseWithUnknownLocation() {
    String request = "{\"businessUnitCode\":\"CI.BAD.001\","
        + "\"location\":\"UNKNOWN\",\"capacity\":20,\"stock\":10}";

    given()
        .contentType("application/json")
        .body(request)
        .when()
        .post("/warehouse")
        .then()
        .statusCode(400);
  }

  @Test
  void rejectsDuplicateActiveBusinessUnitCode() {
    String request = "{\"businessUnitCode\":\"MWH.001\","
        + "\"location\":\"AMSTERDAM-002\",\"capacity\":10,\"stock\":1}";

    given()
        .contentType("application/json")
        .body(request)
        .when()
        .post("/warehouse")
        .then()
        .statusCode(400);
  }

  @Test
  void rejectsCapacityThatExceedsLocationLimit() {
    String request = "{\"businessUnitCode\":\"CI.CAPACITY.001\","
        + "\"location\":\"AMSTERDAM-002\",\"capacity\":76,\"stock\":1}";

    given()
        .contentType("application/json")
        .body(request)
        .when()
        .post("/warehouse")
        .then()
        .statusCode(400);
  }

  @Test
  void rejectsReplacementWhenStockDoesNotMatch() {
    String request = "{\"location\":\"AMSTERDAM-001\","
        + "\"capacity\":60,\"stock\":4}";

    given()
        .contentType("application/json")
        .body(request)
        .when()
        .post("/warehouse/MWH.012/replacement")
        .then()
        .statusCode(400);
  }

  @Test
  void rejectsArchiveForUnknownWarehouse() {
    given()
        .when()
        .delete("/warehouse/99999999")
        .then()
        .statusCode(404);
  }




}
