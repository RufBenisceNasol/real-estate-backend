package net.java.realEstate.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import net.java.realEstate.model.Tenant;
import java.util.Optional;

// TenantRepository interface
public interface TenantRepository extends JpaRepository<Tenant, Integer> {

    // Custom query method to find a tenant by email
    Optional<Tenant> findByEmail(String email);

    // If needed, you can also create methods for other queries, e.g. by contact number
    // Optional<Tenant> findByContactNumber(String contactNumber);
}
