package net.java.realEstate.repository;

import net.java.realEstate.model.Property;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Add this custom method for finding properties by availability
public interface PropertyRepository extends JpaRepository<Property, Integer> {

    // Method to find properties by availability status
    List<Property> findByAvailability(Property.Availability availability);
}
