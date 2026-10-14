package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.location.LocationGateway;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.ws.rs.WebApplicationException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CreateWarehouseUseCaseTest {

  private InMemoryWarehouseStore store;
  private CreateWarehouseUseCase useCase;

  @BeforeEach
  void setUp() {
    store = new InMemoryWarehouseStore();
    useCase = new CreateWarehouseUseCase(
        store, new WarehouseRules(store, new LocationGateway()));
  }

  @Test
  void createsWarehouseWhenAllRulesPass() {
    Warehouse warehouse = warehouse("NEW.001", "ZWOLLE-001", 30, 10);

    useCase.create(warehouse);

    assertEquals(1, store.getAll().size());
    assertEquals("NEW.001", store.getAll().get(0).businessUnitCode);
    assertNotNull(warehouse.createdAt);
  }

  @Test
  void rejectsDuplicateBusinessUnitCode() {
    store.create(warehouse("NEW.001", "ZWOLLE-001", 30, 10));

    assertThrows(WebApplicationException.class,
        () -> useCase.create(warehouse("NEW.001", "AMSTERDAM-002", 30, 10)));
  }

  @Test
  void rejectsUnknownLocation() {
    assertThrows(WebApplicationException.class,
        () -> useCase.create(warehouse("NEW.001", "NOWHERE-001", 20, 10)));
  }

  @Test
  void rejectsCapacitySmallerThanStock() {
    assertThrows(WebApplicationException.class,
        () -> useCase.create(warehouse("NEW.001", "AMSTERDAM-002", 10, 11)));
  }


  @Test
  void rejectsCapacityAboveLocationLimit() {
    assertThrows(WebApplicationException.class,
        () -> useCase.create(warehouse("NEW.001", "AMSTERDAM-002", 80, 10)));
  }

  @Test
  void rejectsCapacityThatWouldExceedLocationTotal() {
    store.create(warehouse("EXISTING", "AMSTERDAM-002", 40, 10));

    assertThrows(WebApplicationException.class,
        () -> useCase.create(warehouse("NEW.001", "AMSTERDAM-002", 40, 10)));
  }

  @Test
  void rejectsWarehouseWhenLocationLimitIsReached() {
    store.create(warehouse("EXISTING", "ZWOLLE-001", 20, 10));

    assertThrows(WebApplicationException.class,
        () -> useCase.create(warehouse("NEW.001", "ZWOLLE-001", 10, 5)));
  }

  static Warehouse warehouse(String code, String location, int capacity, int stock) {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = code;
    warehouse.location = location;
    warehouse.capacity = capacity;
    warehouse.stock = stock;
    return warehouse;
  }

  static class InMemoryWarehouseStore implements WarehouseStore {
    private final List<Warehouse> warehouses = new ArrayList<>();
    private long nextId = 1;

    @Override
    public List<Warehouse> getAll() {
      List<Warehouse> active = new ArrayList<>();
      for (Warehouse warehouse : warehouses) {
        if (warehouse.archivedAt == null) {
          active.add(warehouse);
        }
      }
      return active;
    }

    @Override
    public void create(Warehouse warehouse) {
      warehouse.id = String.valueOf(nextId++);
      warehouses.add(warehouse);
    }

    @Override
    public void update(Warehouse warehouse) {
      for (int i = 0; i < warehouses.size(); i++) {
        Warehouse current = warehouses.get(i);
        if ((warehouse.id != null && warehouse.id.equals(current.id))
            || warehouse.businessUnitCode.equals(current.businessUnitCode)
                && current.archivedAt == null) {
          warehouses.set(i, warehouse);
          return;
        }
      }
      throw new IllegalArgumentException("Warehouse not found");
    }

    @Override
    public void remove(Warehouse warehouse) {
      warehouses.remove(warehouse);
    }

    @Override
    public Warehouse findByBusinessUnitCode(String code) {
      for (Warehouse warehouse : warehouses) {
        if (code.equals(warehouse.businessUnitCode) && warehouse.archivedAt == null) {
          return warehouse;
        }
      }
      return null;
    }
  }
}
