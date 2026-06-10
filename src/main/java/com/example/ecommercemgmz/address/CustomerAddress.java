package com.example.ecommercemgmz.address;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "customer_addresses")
public class CustomerAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private String label;

    @Column(nullable = false)
    private String recipientName;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String street;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String province;

    @Column(nullable = false)
    private String postalCode;

    private String areaId;

    private BigDecimal latitude;

    private BigDecimal longitude;

    @Column(nullable = false)
    private boolean defaultAddress;

    protected CustomerAddress() {
    }

    public CustomerAddress(Long customerId, String label, String recipientName, String phone, String street, String city, String province, String postalCode, String areaId, BigDecimal latitude, BigDecimal longitude, boolean defaultAddress) {
        this.customerId = customerId;
        this.label = label;
        this.recipientName = recipientName;
        this.phone = phone;
        this.street = street;
        this.city = city;
        this.province = province;
        this.postalCode = postalCode;
        this.areaId = areaId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.defaultAddress = defaultAddress;
    }

    public String formatForOrder() {
        return recipientName + " | " + phone + " | " + street + ", " + city + ", " + province + " " + postalCode;
    }
}
