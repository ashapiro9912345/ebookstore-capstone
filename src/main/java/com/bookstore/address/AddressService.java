package com.bookstore.address;

import com.bookstore.address.dto.AddressRequest;
import com.bookstore.address.dto.AddressResponse;
import com.bookstore.user.CurrentUserService;
import com.bookstore.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Delivery address service (FR-04).
 *
 * MVP scope: list and add only. Address update and delete are classified as
 * Secondary scope and are intentionally not implemented (03-api-design.md §8).
 *
 * All operations are scoped to the authenticated customer.
 */
@Service
public class AddressService {

    private final AddressRepository addressRepository;
    private final CurrentUserService currentUserService;
    private final AddressMapper mapper;

    public AddressService(AddressRepository addressRepository,
                          CurrentUserService currentUserService,
                          AddressMapper mapper) {
        this.addressRepository = addressRepository;
        this.currentUserService = currentUserService;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<AddressResponse> listAddresses() {
        User user = currentUserService.requireCurrentUser();
        return addressRepository.findByUserId(user.getId())
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional
    public AddressResponse createAddress(AddressRequest request) {
        User user = currentUserService.requireCurrentUser();

        Address address = new Address();
        address.setUser(user);
        address.setRecipientName(request.getRecipientName());
        address.setStreetLine1(request.getStreetLine1());
        address.setStreetLine2(request.getStreetLine2());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPostalCode(request.getPostalCode());
        address.setCountry(request.getCountry());

        Address saved = addressRepository.save(address);
        return mapper.toResponse(saved);
    }
}
