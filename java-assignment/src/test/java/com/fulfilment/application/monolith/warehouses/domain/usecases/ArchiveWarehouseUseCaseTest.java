package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import org.junit.jupiter.api.Test;

class ArchiveWarehouseUseCaseTest {

  @Test
  void archivesAnExistingWarehouse() {
    CreateWarehouseUseCaseTest.InMemoryWarehouseStore store =
        new CreateWarehouseUseCaseTest.InMemoryWarehouseStore();
    Warehouse warehouse = CreateWarehouseUseCaseTest.warehouse(
        "MWH.TEST", "AMSTERDAM-002", 20, 10);
    store.create(warehouse);

    new ArchiveWarehouseUseCase(store).archive(warehouse);

    assertNotNull(warehouse.archivedAt);
  }

  @Test
  void rejectsNullWarehouse() {
    CreateWarehouseUseCaseTest.InMemoryWarehouseStore store =
        new CreateWarehouseUseCaseTest.InMemoryWarehouseStore();

    assertThrows(jakarta.ws.rs.NotFoundException.class,
        () -> new ArchiveWarehouseUseCase(store).archive(null));
  }
}
