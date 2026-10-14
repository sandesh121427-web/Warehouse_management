package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final WarehouseRules warehouseRules;

  @Inject
  public CreateWarehouseUseCase(WarehouseStore warehouseStore, WarehouseRules warehouseRules) {
    this.warehouseStore = warehouseStore;
    this.warehouseRules = warehouseRules;
  }

  public CreateWarehouseUseCase(WarehouseStore warehouseStore) {
    this(warehouseStore,
        new WarehouseRules(warehouseStore, new com.fulfilment.application.monolith.location.LocationGateway()));
  }

  @Override
  @Transactional
  public void create(Warehouse warehouse) {
    warehouseRules.validateForCreation(warehouse);
    if (warehouse.createdAt == null) {
      warehouse.createdAt = LocalDateTime.now();
    }
    warehouse.archivedAt = null;
    warehouseStore.create(warehouse);
  }
}
