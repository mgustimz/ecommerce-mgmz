package com.example.ecommercemgmz.address;

import java.math.BigDecimal;

public record AddressResponse(
        Long id,
        String label,
        String recipientName,
        String phone,
        String street,
        String city,
        String province,
        String postalCode,
        String areaId,
        BigDecimal latitude,
        BigDecimal longitude,
        boolean defaultAddress
) {
    public static AddressResponse from(CustomerAddress address) {
        return new AddressResponse(
                address.getId(),
                address.getLabel(),
                address.getRecipientName(),
                address.getPhone(),
                address.getStreet(),
                address.getCity(),
                address.getProvince(),
                address.getPostalCode(),
                address.getAreaId(),
                address.getLatitude(),
                address.getLongitude(),
                address.isDefaultAddress()
        );
    }
}
