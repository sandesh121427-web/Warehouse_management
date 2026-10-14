package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.location.LocationGateway;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReplaceWarehouseUseCaseTest {

  private CreateWarehouseUseCaseTest.InMemoryWarehouseStore store;
  private ReplaceWarehouseUseCase useCase;

  @BeforeEach
  void setUp() {
    store = new CreateWarehouseUseCaseTest.InMemoryWarehouseStore();
    WarehouseRules rules = new WarehouseRules(store, new LocationGateway());
    useCase = new ReplaceWarehouseUseCase(store, rules);
  }

  @Test
  void archivesOldWarehouseAndCreatesReplacementWithSameBusinessCode() {
    Warehouse current = CreateWarehouseUseCaseTest.warehouse(
        "MWH.TEST", "AMSTERDAM-002", 20, 10);
    store.create(current);

    Warehouse replacement = CreateWarehouseUseCaseTest.warehouse(
        "MWH.TEST", "AMSTERDAM-002", 25, 10);
    useCase.replace(replacement);

    assertNotNull(current.archivedAt);
    assertEquals(1, store.getAll().size());
    assertEquals("MWH.TEST", store.getAll().get(0).businessUnitCode);
    assertEquals(Integer.valueOf(25), store.getAll().get(0).capacity);
    assertEquals(Integer.valueOf(10), store.getAll().get(0).stock);
  }

  @Test
  void rejectsReplacementWhenStockDoesNotMatch() {
    store.create(CreateWarehouseUseCaseTest.warehouse(
        "MWH.TEST", "AMSTERDAM-002", 20, 10));

    Warehouse replacement = CreateWarehouseUseCaseTest.warehouse(
        "MWH.TEST", "AMSTERDAM-002", 25, 9);

    assertThrows(WebApplicationException.class, () -> useCase.replace(replacement));
    assertEquals(1, store.getAll().size());
  }

  @Test
  void rejectsReplacementWhenWarehouseDoesNotExist() {
    Warehouse replacement = CreateWarehouseUseCaseTest.warehouse(
        "MISSING", "AMSTERDAM-002", 25, 10);

    assertThrows(jakarta.ws.rs.NotFoundException.class, () -> useCase.replace(replacement));
  }
}
