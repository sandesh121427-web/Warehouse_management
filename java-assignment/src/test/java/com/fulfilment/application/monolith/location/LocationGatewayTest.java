package com.fulfilment.application.monolith.location;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import org.junit.jupiter.api.Test;

class LocationGatewayTest {

  private final LocationGateway locationGateway = new LocationGateway();

  @Test
  void resolvesAnExistingLocation() {
    Location location = locationGateway.resolveByIdentifier("ZWOLLE-001");

    assertEquals("ZWOLLE-001", location.identification);
    assertEquals(1, location.maxNumberOfWarehouses);
    assertEquals(40, location.maxCapacity);
  }

  @Test
  void lookupIsCaseInsensitiveAndTrimsWhitespace() {
    assertEquals("AMSTERDAM-001",
        locationGateway.resolveByIdentifier(" amsterdam-001 ").identification);
  }

  @Test
  void returnsNullForUnknownOrBlankIdentifier() {
    assertNull(locationGateway.resolveByIdentifier("UNKNOWN"));
    assertNull(locationGateway.resolveByIdentifier(" "));
    assertNull(locationGateway.resolveByIdentifier(null));
  }
}
