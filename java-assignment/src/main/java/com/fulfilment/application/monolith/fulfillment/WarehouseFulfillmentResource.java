package com.fulfilment.application.monolith.fulfillment;

import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.products.ProductRepository;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Optional;

@Path("/fulfillment-assignment")
@ApplicationScoped
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class WarehouseFulfillmentResource {

  @Inject ProductRepository productRepository;
  @Inject WarehouseRepository warehouseRepository;
  @Inject WarehouseFulfillmentRepository fulfillmentRepository;

  @POST
  @Transactional
  public Response assignWarehouse(AssignmentRequest request) {
    if (request == null || request.storeId == null || request.productId == null
        || request.warehouseBusinessUnitCode == null
        || request.warehouseBusinessUnitCode.trim().isEmpty()) {
      throw new WebApplicationException(
          "storeId, productId and warehouseBusinessUnitCode are required.", 400);
    }

    Store store = Store.findById(request.storeId);
    if (store == null) {
      throw new NotFoundException("Store " + request.storeId + " does not exist.");
    }

    Product product = productRepository.findById(request.productId);
    if (product == null) {
      throw new NotFoundException("Product " + request.productId + " does not exist.");
    }

    com.fulfilment.application.monolith.warehouses.domain.models.Warehouse activeWarehouse =
        warehouseRepository.findByBusinessUnitCode(request.warehouseBusinessUnitCode);
    if (activeWarehouse == null || activeWarehouse.id == null) {
      throw new NotFoundException(
          "Active warehouse " + request.warehouseBusinessUnitCode + " does not exist.");
    }

    DbWarehouse warehouse = warehouseRepository.findById(Long.valueOf(activeWarehouse.id));
    Optional<WarehouseFulfillment> existing =
        fulfillmentRepository.findAssignment(store, product, warehouse);
    if (existing.isPresent()) {
      return Response.ok(toResponse(existing.get())).build();
    }

    if (fulfillmentRepository.countWarehousesForProductAndStore(store, product) >= 2) {
      throw new WebApplicationException(
          "A product can be fulfilled by at most two warehouses per store.", 400);
    }
    if (fulfillmentRepository.countWarehousesForStore(store) >= 3) {
      throw new WebApplicationException(
          "A store can be fulfilled by at most three different warehouses.", 400);
    }
    if (fulfillmentRepository.countProductsForWarehouse(warehouse) >= 5) {
      throw new WebApplicationException(
          "A warehouse can store at most five different product types.", 400);
    }

    WarehouseFulfillment assignment = new WarehouseFulfillment();
    assignment.store = store;
    assignment.product = product;
    assignment.warehouse = warehouse;
    fulfillmentRepository.persist(assignment);
    return Response.status(Response.Status.CREATED).entity(toResponse(assignment)).build();
  }

  private AssignmentResponse toResponse(WarehouseFulfillment assignment) {
    AssignmentResponse response = new AssignmentResponse();
    response.assignmentId = assignment.id;
    response.storeId = assignment.store.id;
    response.productId = assignment.product.id;
    response.warehouseBusinessUnitCode = assignment.warehouse.businessUnitCode;
    return response;
  }

  public static class AssignmentRequest {
    public Long storeId;
    public Long productId;
    public String warehouseBusinessUnitCode;
  }

  public static class AssignmentResponse {
    public Long assignmentId;
    public Long storeId;
    public Long productId;
    public String warehouseBusinessUnitCode;
  }
}
