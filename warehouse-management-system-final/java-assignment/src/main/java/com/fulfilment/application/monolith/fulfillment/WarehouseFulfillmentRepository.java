package com.fulfilment.application.monolith.fulfillment;

import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class WarehouseFulfillmentRepository implements PanacheRepository<WarehouseFulfillment> {

  public Optional<WarehouseFulfillment> findAssignment(
      Store store, Product product, DbWarehouse warehouse) {
    return find("store = ?1 and product = ?2 and warehouse = ?3", store, product, warehouse)
        .firstResultOptional();
  }

  public long countWarehousesForProductAndStore(Store store, Product product) {
    return getEntityManager()
        .createQuery(
            "select count(distinct f.warehouse.id) from WarehouseFulfillment f "
                + "where f.store.id = :storeId and f.product.id = :productId",
            Long.class)
        .setParameter("storeId", store.id)
        .setParameter("productId", product.id)
        .getSingleResult();
  }

  public long countWarehousesForStore(Store store) {
    return getEntityManager()
        .createQuery(
            "select count(distinct f.warehouse.id) from WarehouseFulfillment f "
                + "where f.store.id = :storeId",
            Long.class)
        .setParameter("storeId", store.id)
        .getSingleResult();
  }

  public long countProductsForWarehouse(DbWarehouse warehouse) {
    return getEntityManager()
        .createQuery(
            "select count(distinct f.product.id) from WarehouseFulfillment f "
                + "where f.warehouse.id = :warehouseId",
            Long.class)
        .setParameter("warehouseId", warehouse.id)
        .getSingleResult();
  }
}
