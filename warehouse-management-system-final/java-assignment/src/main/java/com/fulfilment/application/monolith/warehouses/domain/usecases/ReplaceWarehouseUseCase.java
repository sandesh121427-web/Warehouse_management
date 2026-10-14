package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import java.time.LocalDateTime;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final WarehouseRules warehouseRules;

  @Inject
  public ReplaceWarehouseUseCase(WarehouseStore warehouseStore, WarehouseRules warehouseRules) {
    this.warehouseStore = warehouseStore;
    this.warehouseRules = warehouseRules;
  }

  public ReplaceWarehouseUseCase(WarehouseStore warehouseStore) {
    this(warehouseStore,
        new WarehouseRules(warehouseStore, new com.fulfilment.application.monolith.location.LocationGateway()));
  }

  @Override
  @Transactional
  public void replace(Warehouse newWarehouse) {
    if (newWarehouse == null || newWarehouse.businessUnitCode == null) {
      throw new jakarta.ws.rs.WebApplicationException("Business unit code is required.", 400);
    }

    Warehouse current = warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode);
    if (current == null) {
      throw new NotFoundException(
          "Active warehouse not found: " + newWarehouse.businessUnitCode);
    }

    warehouseRules.validateForReplacement(newWarehouse, current);

    current.archivedAt = LocalDateTime.now();
    warehouseStore.update(current);

    newWarehouse.id = null;
    newWarehouse.createdAt = LocalDateTime.now();
    newWarehouse.archivedAt = null;
    warehouseStore.create(newWarehouse);
  }
}
