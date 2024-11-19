package net.java.realEstate.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import net.java.realEstate.model.Tenant;

// TenantRepository interface
public interface TenantRepository extends JpaRepository<Tenant, Integer> {
    // Custom query methods (if needed) can be added here
}
