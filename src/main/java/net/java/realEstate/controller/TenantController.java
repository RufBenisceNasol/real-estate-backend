package net.java.realEstate.controller;

import net.java.realEstate.exception.ResourceNotFoundException;
import net.java.realEstate.model.Tenant;
import net.java.realEstate.repository.TenantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // Injected PasswordEncoder

    // Get all tenants
    @GetMapping
    public List<Tenant> getAllTenants() {
        return tenantRepository.findAll();
    }

    // Get tenant by ID
    @GetMapping("/{id}")
    public ResponseEntity<Tenant> getTenantById(@PathVariable int id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + id));
        return ResponseEntity.ok(tenant);
    }

    // Create a new tenant
    @PostMapping
    public Tenant createTenant(@RequestBody Tenant tenant) {
        // Hash the password before saving to the database
        String hashedPassword = passwordEncoder.encode(tenant.getPassword());
        tenant.setPassword(hashedPassword);

        // Save the tenant to the database
        return tenantRepository.save(tenant);
    }

    // Update an existing tenant
    @PutMapping("/{id}")
    public ResponseEntity<Tenant> updateTenant(@PathVariable int id, @RequestBody Tenant tenantDetails) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + id));

        tenant.setName(tenantDetails.getName());
        tenant.setEmail(tenantDetails.getEmail());
        tenant.setContactNumber(tenantDetails.getContactNumber());  // Changed from "setPhone" to "setContactNumber"

        // Hash the password if it's being updated
        if (tenantDetails.getPassword() != null && !tenantDetails.getPassword().isEmpty()) {
            String hashedPassword = passwordEncoder.encode(tenantDetails.getPassword());
            tenant.setPassword(hashedPassword);
        }

        // Save the updated tenant to the database
        Tenant updatedTenant = tenantRepository.save(tenant);
        return ResponseEntity.ok(updatedTenant);
    }

    // Delete a tenant
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTenant(@PathVariable int id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + id));

        tenantRepository.delete(tenant);
        return ResponseEntity.noContent().build();
    }
}
