package com.fulfilment.application.monolith.stores;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

@QuarkusTest
class StoreAfterCommitTest {

  @InjectMock LegacyStoreManagerGateway legacyGateway;

  @Test
  void createSynchronizesOnlyAfterStoreIsCommitted() {
    AtomicBoolean persistedWhenLegacyWasCalled = new AtomicBoolean(false);
    doAnswer(invocation -> {
      Store snapshot = invocation.getArgument(0);
      persistedWhenLegacyWasCalled.set(snapshot.id != null && Store.findById(snapshot.id) != null);
      return null;
    }).when(legacyGateway).createStoreOnLegacySystem(any(Store.class));

    given()
        .contentType("application/json")
        .body("{\"name\":\"CI_STORE_COMMIT_TEST\","
            + "\"quantityProductsInStock\":7}")
        .when()
        .post("/store")
        .then()
        .statusCode(201);

    verify(legacyGateway).createStoreOnLegacySystem(any(Store.class));
    assertTrue(persistedWhenLegacyWasCalled.get(),
        "The legacy gateway must only receive a store after its transaction commits");
  }


  @Test
  void updateSynchronizesOnlyAfterStoreIsCommitted() {
    AtomicBoolean persistedWhenLegacyWasCalled = new AtomicBoolean(false);
    doAnswer(invocation -> {
      Store snapshot = invocation.getArgument(0);
      Store persisted = snapshot.id == null ? null : Store.findById(snapshot.id);
      persistedWhenLegacyWasCalled.set(persisted != null
          && "CI_STORE_UPDATE_TEST_UPDATED".equals(persisted.name));
      return null;
    }).when(legacyGateway).updateStoreOnLegacySystem(any(Store.class));

    String createBody = "{\"name\":\"CI_STORE_UPDATE_TEST\","
        + "\"quantityProductsInStock\":3}";
    String id = given()
        .contentType("application/json")
        .body(createBody)
        .when()
        .post("/store")
        .then()
        .statusCode(201)
        .extract()
        .jsonPath()
        .getString("id");

    given()
        .contentType("application/json")
        .body("{\"name\":\"CI_STORE_UPDATE_TEST_UPDATED\","
            + "\"quantityProductsInStock\":4}")
        .when()
        .put("/store/" + id)
        .then()
        .statusCode(200);

    verify(legacyGateway).updateStoreOnLegacySystem(any(Store.class));
    assertTrue(persistedWhenLegacyWasCalled.get(),
        "The legacy gateway must receive the updated row after commit");
  }

  @Test
  void invalidCreateDoesNotSynchronizeWithLegacySystem() {
    given()
        .contentType("application/json")
        .body("{\"id\":999,\"name\":\"INVALID_STORE\","
            + "\"quantityProductsInStock\":1}")
        .when()
        .post("/store")
        .then()
        .statusCode(422);

    verifyNoInteractions(legacyGateway);
  }
}
