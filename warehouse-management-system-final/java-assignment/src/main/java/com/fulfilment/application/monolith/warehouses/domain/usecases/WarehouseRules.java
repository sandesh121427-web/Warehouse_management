package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import java.util.List;
import java.util.Objects;

@ApplicationScoped
public class WarehouseRules {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  @Inject
  public WarehouseRules(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  public void validateForCreation(Warehouse warehouse) {
    validateShape(warehouse);
    if (warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode) != null) {
      throw badRequest("Business unit code already exists: " + warehouse.businessUnitCode);
    }
    validateLocationAndCapacity(warehouse, null);
  }

  public void validateForReplacement(Warehouse replacement, Warehouse previous) {
    validateShape(replacement);
    if (!previous.businessUnitCode.equals(replacement.businessUnitCode)) {
      throw badRequest("Replacement must reuse business unit code " + previous.businessUnitCode);
    }
    if (previous.stock == null || !previous.stock.equals(replacement.stock)) {
      throw badRequest("Replacement stock must match the current warehouse stock.");
    }
    if (previous.stock == null || replacement.capacity < previous.stock) {
      throw badRequest("Replacement capacity cannot accommodate the current stock.");
    }
    validateLocationAndCapacity(replacement, previous);
  }

  private void validateShape(Warehouse warehouse) {
    if (warehouse == null) {
      throw badRequest("Warehouse request body is required.");
    }
    if (warehouse.businessUnitCode == null || warehouse.businessUnitCode.trim().isEmpty()) {
      throw badRequest("Business unit code is required.");
    }
    if (warehouse.location == null || warehouse.location.trim().isEmpty()) {
      throw badRequest("Location is required.");
    }
    if (warehouse.capacity == null || warehouse.capacity <= 0) {
      throw badRequest("Capacity must be greater than zero.");
    }
    if (warehouse.stock == null || warehouse.stock < 0) {
      throw badRequest("Stock cannot be negative.");
    }
    if (warehouse.capacity < warehouse.stock) {
      throw badRequest("Warehouse capacity must be greater than or equal to stock.");
    }
  }

  private void validateLocationAndCapacity(Warehouse warehouse, Warehouse excludedWarehouse) {
    Location location = locationResolver.resolveByIdentifier(warehouse.location);
    if (location == null) {
      throw badRequest("Unknown warehouse location: " + warehouse.location);
    }

    if (warehouse.capacity > location.maxCapacity) {
      throw badRequest("Warehouse capacity exceeds the maximum capacity for location "
          + location.identification + ".");
    }

    List<Warehouse> activeWarehouses = warehouseStore.getAll();
    int warehouseCount = 0;
    int totalCapacity = 0;

    for (Warehouse current : activeWarehouses) {
      if (excludedWarehouse != null
          && (sameId(current, excludedWarehouse)
              || Objects.equals(current.businessUnitCode, excludedWarehouse.businessUnitCode))) {
        continue;
      }
      if (location.identification.equalsIgnoreCase(current.location)) {
        warehouseCount++;
        totalCapacity += current.capacity == null ? 0 : current.capacity;
      }
    }

    if (warehouseCount >= location.maxNumberOfWarehouses) {
      throw badRequest("Maximum number of warehouses reached for location "
          + location.identification + ".");
    }

    if (totalCapacity + warehouse.capacity > location.maxCapacity) {
      throw badRequest("Total warehouse capacity would exceed the limit for location "
          + location.identification + ".");
    }
  }

  private boolean sameId(Warehouse first, Warehouse second) {
    return first.id != null && first.id.equals(second.id);
  }

  private WebApplicationException badRequest(String message) {
    return new WebApplicationException(message, 400);
  }
}
