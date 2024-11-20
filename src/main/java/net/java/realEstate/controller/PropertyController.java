package net.java.realEstate.controller;

import net.java.realEstate.model.ErrorResponse;
import net.java.realEstate.model.Property;
import net.java.realEstate.repository.PropertyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/properties")
@Validated
public class PropertyController {

    @Autowired
    private PropertyRepository propertyRepository;

    // Endpoint to add a new property
    @PostMapping
    public ResponseEntity<?> createProperty(@Valid @RequestBody Property property, BindingResult result) {
        if (result.hasErrors()) {
            String errorMessage = result.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    "Validation failed: " + errorMessage,
                    System.currentTimeMillis());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        try {
            Property savedProperty = propertyRepository.save(property);
            return new ResponseEntity<>(savedProperty, HttpStatus.CREATED);
        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Internal Server Error: " + e.getMessage(),
                    System.currentTimeMillis());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // Endpoint to get all properties
    @GetMapping
    public ResponseEntity<List<Property>> getAllProperties() {
        try {
            List<Property> properties = propertyRepository.findAll();
            if (properties.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT); // Return 204 if no properties are found
            }
            return new ResponseEntity<>(properties, HttpStatus.OK);
        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Internal Server Error: " + e.getMessage(),
                    System.currentTimeMillis());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Endpoint to get a single property by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getPropertyById(@PathVariable int id) {
        Optional<Property> property = propertyRepository.findById(id);

        if (property.isPresent()) {
            return new ResponseEntity<>(property.get(), HttpStatus.OK); // Return property if found
        } else {
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.NOT_FOUND.value(),
                    "Property not found with ID: " + id,
                    System.currentTimeMillis());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse); // Return 404 if not found
        }
    }

    // Endpoint to get all available properties
    @GetMapping("/available")
    public ResponseEntity<List<Property>> getAllAvailableProperties() {
        try {
            List<Property> availableProperties = propertyRepository.findByAvailability(Property.Availability.AVAILABLE);
            if (availableProperties.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT); // Return 204 if no available properties are found
            }
            return new ResponseEntity<>(availableProperties, HttpStatus.OK);
        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Internal Server Error: " + e.getMessage(),
                    System.currentTimeMillis());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Endpoint to update an existing property by ID
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProperty(@PathVariable int id, @Valid @RequestBody Property property) {
        Optional<Property> existingProperty = propertyRepository.findById(id);

        if (existingProperty.isPresent()) {
            Property currentProperty = existingProperty.get();

            // Update fields only if they are provided (non-null)
            if (property.getName() != null && !property.getName().isEmpty()) {
                currentProperty.setName(property.getName());
            }
            if (property.getAddress() != null && !property.getAddress().isEmpty()) {
                currentProperty.setAddress(property.getAddress());
            }
            if (property.getPrice() > 0) {
                currentProperty.setPrice(property.getPrice());
            }
            if (property.getAvailability() != null) {
                currentProperty.setAvailability(property.getAvailability());
            }
            if (property.getType() != null) {
                currentProperty.setType(property.getType());
            }

            // Save the updated property to the database
            Property updatedProperty = propertyRepository.save(currentProperty);
            return new ResponseEntity<>(updatedProperty, HttpStatus.OK);
        } else {
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.NOT_FOUND.value(),
                    "Property not found with ID: " + id,
                    System.currentTimeMillis());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProperty(@PathVariable int id) {
        Optional<Property> existingPropertyOpt = propertyRepository.findById(id);

        if (!existingPropertyOpt.isPresent()) {
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.NOT_FOUND.value(),
                    "Property not found with ID: " + id,
                    System.currentTimeMillis());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }

        propertyRepository.deleteById(id); // Delete the property

        // Custom success response message
        String successMessage = "Property with ID: " + id + " was successfully deleted.";
        return new ResponseEntity<>(
                new ErrorResponse(HttpStatus.OK.value(), successMessage, System.currentTimeMillis()), HttpStatus.OK);
    }

    // Handle validation exceptions (MethodArgumentNotValidException)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        BindingResult result = ex.getBindingResult();
        String errorMessage = result.getAllErrors().stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed: " + errorMessage,
                System.currentTimeMillis());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    // Handle general exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralExceptions(Exception ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred: " + ex.getMessage(),
                System.currentTimeMillis());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
