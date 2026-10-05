package com.bookstore.address;

import com.bookstore.address.dto.AddressResponse;
import org.springframework.stereotype.Component;

/**
 * Hand-written mapper from Address entity to AddressResponse DTO.
 */
@Component
public class AddressMapper {

    public AddressResponse toResponse(Address address) {
        return new AddressResponse(
                address.getId(),
                address.getRecipientName(),
                address.getStreetLine1(),
                address.getStreetLine2(),
                address.getCity(),
                address.getState(),
                address.getPostalCode(),
                address.getCountry(),
                address.getCreatedAt());
    }
}
