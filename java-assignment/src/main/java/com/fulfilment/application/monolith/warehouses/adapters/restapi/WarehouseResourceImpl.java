package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

/**
 * HTTP adapter for warehouse operations. The domain model stays independent of the API
 * contract; this resource maps request/response DTOs to the use-case model.
 */
@Path("/warehouse")
@RequestScoped
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class WarehouseResourceImpl {

  @Inject WarehouseRepository warehouseRepository;
  @Inject CreateWarehouseOperation createWarehouseOperation;
  @Inject ReplaceWarehouseOperation replaceWarehouseOperation;
  @Inject ArchiveWarehouseOperation archiveWarehouseOperation;

  @GET
  public List<WarehouseResponse> listAllWarehousesUnits() {
    return warehouseRepository.getAll().stream()
        .map(this::toWarehouseResponse)
        .collect(Collectors.toList());
  }

  @POST
  @Transactional
  public Response createANewWarehouseUnit(WarehouseRequest data) {
    Warehouse warehouse = toDomainWarehouse(data);
    createWarehouseOperation.create(warehouse);
    return Response.status(Response.Status.CREATED)
        .entity(toWarehouseResponse(warehouse))
        .build();
  }

  @GET
  @Path("/{id}")
  public WarehouseResponse getAWarehouseUnitByID(@PathParam("id") String id) {
    Warehouse warehouse = warehouseRepository.findByDatabaseId(id);
    if (warehouse == null) {
      throw new NotFoundException("Warehouse with id " + id + " does not exist.");
    }
    return toWarehouseResponse(warehouse);
  }

  @DELETE
  @Path("/{id}")
  @Transactional
  public Response archiveAWarehouseUnitByID(@PathParam("id") String id) {
    Warehouse warehouse = warehouseRepository.findByDatabaseId(id);
    if (warehouse == null) {
      throw new NotFoundException("Warehouse with id " + id + " does not exist.");
    }
    archiveWarehouseOperation.archive(warehouse);
    return Response.noContent().build();
  }

  @POST
  @Path("/{businessUnitCode}/replacement")
  @Transactional
  public WarehouseResponse replaceTheCurrentActiveWarehouse(
      @PathParam("businessUnitCode") String businessUnitCode, WarehouseRequest data) {
    if (businessUnitCode == null || businessUnitCode.trim().isEmpty()) {
      throw new WebApplicationException("Business unit code is required.", 400);
    }
    Warehouse warehouse = toDomainWarehouse(data);
    warehouse.businessUnitCode = businessUnitCode;
    replaceWarehouseOperation.replace(warehouse);
    return toWarehouseResponse(warehouse);
  }

  private Warehouse toDomainWarehouse(WarehouseRequest source) {
    if (source == null) {
      throw new WebApplicationException("Warehouse request body is required.", 400);
    }
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = source.businessUnitCode;
    warehouse.location = source.location;
    warehouse.capacity = source.capacity;
    warehouse.stock = source.stock;
    return warehouse;
  }

  private WarehouseResponse toWarehouseResponse(Warehouse warehouse) {
    WarehouseResponse response = new WarehouseResponse();
    response.id = warehouse.id;
    response.businessUnitCode = warehouse.businessUnitCode;
    response.location = warehouse.location;
    response.capacity = warehouse.capacity;
    response.stock = warehouse.stock;
    return response;
  }

  public static class WarehouseRequest {
    public String businessUnitCode;
    public String location;
    public Integer capacity;
    public Integer stock;
  }

  public static class WarehouseResponse {
    public String id;
    public String businessUnitCode;
    public String location;
    public Integer capacity;
    public Integer stock;
  }
}
