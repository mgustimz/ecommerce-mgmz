package com.example.ecommercemgmz.address;

import com.example.ecommercemgmz.common.ApiException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AddressService {
    private final CustomerAddressRepository addressRepository;

    public AddressService(CustomerAddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    @Transactional(readOnly = true)
    public List<AddressResponse> findCustomerAddresses(Long customerId) {
        return addressRepository.findByCustomerIdOrderByDefaultAddressDescIdDesc(customerId).stream()
                .map(AddressResponse::from)
                .toList();
    }

    @Transactional
    public AddressResponse create(Long customerId, AddressRequest request) {
        boolean makeDefault = request.defaultAddress() || addressRepository.findByCustomerIdOrderByDefaultAddressDescIdDesc(customerId).isEmpty();
        if (makeDefault) {
            clearDefault(customerId);
        }
        CustomerAddress address = new CustomerAddress(
                customerId,
                request.label(),
                request.recipientName(),
                request.phone(),
                request.street(),
                request.city(),
                request.province(),
                request.postalCode(),
                request.areaId(),
                request.latitude(),
                request.longitude(),
                makeDefault
        );
        return AddressResponse.from(addressRepository.save(address));
    }

    @Transactional
    public AddressResponse update(Long customerId, Long addressId, AddressRequest request) {
        CustomerAddress address = findEntity(customerId, addressId);
        if (request.defaultAddress()) {
            clearDefault(customerId);
        }
        address.setLabel(request.label());
        address.setRecipientName(request.recipientName());
        address.setPhone(request.phone());
        address.setStreet(request.street());
        address.setCity(request.city());
        address.setProvince(request.province());
        address.setPostalCode(request.postalCode());
        address.setAreaId(request.areaId());
        address.setLatitude(request.latitude());
        address.setLongitude(request.longitude());
        address.setDefaultAddress(request.defaultAddress());
        return AddressResponse.from(address);
    }

    @Transactional
    public AddressResponse setDefault(Long customerId, Long addressId) {
        CustomerAddress address = findEntity(customerId, addressId);
        clearDefault(customerId);
        address.setDefaultAddress(true);
        return AddressResponse.from(address);
    }

    @Transactional
    public void delete(Long customerId, Long addressId) {
        CustomerAddress address = findEntity(customerId, addressId);
        addressRepository.delete(address);
    }

    public CustomerAddress findEntity(Long customerId, Long addressId) {
        return addressRepository.findByIdAndCustomerId(addressId, customerId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Address not found"));
    }

    private void clearDefault(Long customerId) {
        addressRepository.findByCustomerIdOrderByDefaultAddressDescIdDesc(customerId)
                .forEach(address -> address.setDefaultAddress(false));
    }
}
