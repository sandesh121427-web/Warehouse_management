package com.fulfilment.application.monolith.location;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ApplicationScoped
public class LocationGateway implements LocationResolver {

  private static final List<Location> LOCATIONS = createLocations();

  private static List<Location> createLocations() {
    List<Location> locations = new ArrayList<>();
    locations.add(new Location("ZWOLLE-001", 1, 40));
    locations.add(new Location("ZWOLLE-002", 2, 50));
    locations.add(new Location("AMSTERDAM-001", 5, 100));
    locations.add(new Location("AMSTERDAM-002", 3, 75));
    locations.add(new Location("TILBURG-001", 1, 40));
    locations.add(new Location("HELMOND-001", 1, 45));
    locations.add(new Location("EINDHOVEN-001", 2, 70));
    locations.add(new Location("VETSBY-001", 1, 90));
    return Collections.unmodifiableList(locations);
  }

  @Override
  public Location resolveByIdentifier(String identifier) {
    if (identifier == null || identifier.trim().isEmpty()) {
      return null;
    }

    return LOCATIONS.stream()
        .filter(location -> location.identification.equalsIgnoreCase(identifier.trim()))
        .findFirst()
        .orElse(null);
  }
}
