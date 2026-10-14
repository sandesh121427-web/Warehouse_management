package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  @Override
  public List<Warehouse> getAll() {
    return find("archivedAt is null").list().stream()
        .map(DbWarehouse::toWarehouse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public void create(Warehouse warehouse) {
    DbWarehouse entity = DbWarehouse.fromWarehouse(warehouse);
    if (entity.createdAt == null) {
      entity.createdAt = java.time.LocalDateTime.now();
    }
    entity.archivedAt = null;
    persist(entity);
    warehouse.id = entity.id == null ? null : entity.id.toString();
    warehouse.createdAt = entity.createdAt;
    warehouse.archivedAt = null;
  }

  @Override
  @Transactional
  public void update(Warehouse warehouse) {
    DbWarehouse entity = findEntity(warehouse);
    if (entity == null) {
      throw new jakarta.ws.rs.NotFoundException(
          "Warehouse " + warehouse.businessUnitCode + " does not exist.");
    }

    entity.businessUnitCode = warehouse.businessUnitCode;
    entity.location = warehouse.location;
    entity.capacity = warehouse.capacity;
    entity.stock = warehouse.stock;
    entity.createdAt = warehouse.createdAt == null ? entity.createdAt : warehouse.createdAt;
    entity.archivedAt = warehouse.archivedAt;
  }

  @Override
  @Transactional
  public void remove(Warehouse warehouse) {
    DbWarehouse entity = findEntity(warehouse);
    if (entity != null) {
      delete(entity);
    }
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    if (buCode == null) {
      return null;
    }
    return find("businessUnitCode = ?1 and archivedAt is null", buCode)
        .firstResultOptional()
        .map(DbWarehouse::toWarehouse)
        .orElse(null);
  }

  public Warehouse findByDatabaseId(String id) {
    if (id == null || !id.matches("\\d+")) {
      return null;
    }
    return find("id = ?1 and archivedAt is null", Long.valueOf(id))
        .firstResultOptional()
        .map(DbWarehouse::toWarehouse)
        .orElse(null);
  }

  private DbWarehouse findEntity(Warehouse warehouse) {
    if (warehouse.id != null && warehouse.id.matches("\\d+")) {
      Optional<DbWarehouse> byId = findByIdOptional(Long.valueOf(warehouse.id));
      if (byId.isPresent()) {
        return byId.get();
      }
    }
    if (warehouse.businessUnitCode == null) {
      return null;
    }
    return find("businessUnitCode = ?1 and archivedAt is null", warehouse.businessUnitCode)
        .firstResult();
  }
}
