package com.bookstore.address;

import com.bookstore.address.dto.AddressRequest;
import com.bookstore.address.dto.AddressResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Delivery address endpoints (authenticated; own addresses only).
 *
 * Implements:
 *   GET  /api/addresses — list the customer's saved addresses
 *   POST /api/addresses — add a new delivery address (201 Created)
 */
@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping
    public ResponseEntity<List<AddressResponse>> listAddresses() {
        return ResponseEntity.ok(addressService.listAddresses());
    }

    @PostMapping
    public ResponseEntity<AddressResponse> createAddress(
            @Valid @RequestBody AddressRequest request) {
        AddressResponse created = addressService.createAddress(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
