package net.java.realEstate.controller;

import net.java.realEstate.exception.ResourceNotFoundException;
import net.java.realEstate.model.Tenant;
import net.java.realEstate.repository.TenantRepository;
import net.java.realEstate.dto.PasswordUpdateDTO;
import net.java.realEstate.dto.ProfileUpdateDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
    public ResponseEntity<?> createTenant(@Valid @RequestBody Tenant tenant, BindingResult result) {
        if (result.hasErrors()) {
            StringBuilder errorMessage = new StringBuilder();
            result.getAllErrors().forEach(error -> errorMessage.append(error.getDefaultMessage()).append(" "));
            return ResponseEntity.badRequest().body(
                    new ErrorResponse("Validation Failed", errorMessage.toString(), HttpStatus.BAD_REQUEST.value()));
        }

        // Hash the password before saving to the database
        String hashedPassword = passwordEncoder.encode(tenant.getPassword());
        tenant.setPassword(hashedPassword);

        // Save the tenant to the database
        Tenant savedTenant = tenantRepository.save(tenant);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedTenant);
    }

    // Edit Profile (name, email, contact number)
    @PutMapping("/{id}/edit-profile")
    public ResponseEntity<?> editProfile(@PathVariable int id, @Valid @RequestBody ProfileUpdateDTO profileUpdateDTO,
            BindingResult result) {
        if (result.hasErrors()) {
            StringBuilder errorMessages = new StringBuilder();
            result.getAllErrors().forEach(error -> errorMessages.append(error.getDefaultMessage()).append(" "));
            return ResponseEntity.badRequest().body(
                    new ErrorResponse("Validation Failed", errorMessages.toString(), HttpStatus.BAD_REQUEST.value()));
        }

        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + id));

        // Update fields only if the new values are not null or empty
        if (profileUpdateDTO.getName() != null && !profileUpdateDTO.getName().isEmpty()) {
            tenant.setName(profileUpdateDTO.getName());
        }
        if (profileUpdateDTO.getEmail() != null && !profileUpdateDTO.getEmail().isEmpty()) {
            tenant.setEmail(profileUpdateDTO.getEmail());
        }
        if (profileUpdateDTO.getContactNumber() != null && profileUpdateDTO.getContactNumber() > 0) {
            tenant.setContactNumber(profileUpdateDTO.getContactNumber());
        }

        // Save the updated tenant to the database
        Tenant updatedTenant = tenantRepository.save(tenant);
        return ResponseEntity.ok(updatedTenant);
    }

    // Update Password
    @PutMapping("/{id}/update-password")
    public ResponseEntity<?> updatePassword(@PathVariable int id,
            @Valid @RequestBody PasswordUpdateDTO passwordUpdateDTO, BindingResult result) {
        if (result.hasErrors()) {
            StringBuilder errorMessages = new StringBuilder();
            result.getAllErrors().forEach(error -> errorMessages.append(error.getDefaultMessage()).append(" "));
            return ResponseEntity.badRequest().body(
                    new ErrorResponse("Validation Failed", errorMessages.toString(), HttpStatus.BAD_REQUEST.value()));
        }

        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + id));

        // Check if the old password matches
        if (!passwordEncoder.matches(passwordUpdateDTO.getOldPassword(), tenant.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Unauthorized", "Old password is incorrect",
                            HttpStatus.UNAUTHORIZED.value()));
        }

        // Hash the new password and set it
        String hashedPassword = passwordEncoder.encode(passwordUpdateDTO.getNewPassword());
        tenant.setPassword(hashedPassword);

        // Save the updated tenant to the database
        Tenant updatedTenant = tenantRepository.save(tenant);
        return ResponseEntity.ok(updatedTenant);
    }

    // Delete a tenant
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTenant(@PathVariable int id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + id));

        tenantRepository.delete(tenant);

        // Return success response with a message
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse("Tenant deleted successfully", HttpStatus.OK.value()));
    }

    // Global exception handler for ResourceNotFoundException
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFound(ResourceNotFoundException ex) {
        return new ResponseEntity<>(
                new ErrorResponse("Tenant Not Found", ex.getMessage(), HttpStatus.NOT_FOUND.value()),
                HttpStatus.NOT_FOUND);
    }

    // Error response structure
    public static class ErrorResponse {
        private String error;
        private String message;
        private int status;

        public ErrorResponse(String error, String message, int status) {
            this.error = error;
            this.message = message;
            this.status = status;
        }

        public String getError() { 
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }
    }

    // Success response structure
public static class SuccessResponse {
    private String message;
    private int status;

    public SuccessResponse(String message, int status) {
        this.message = message;
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
}
