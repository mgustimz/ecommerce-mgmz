package com.example.ecommercemgmz.shipping;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BiteshipAreaResponse(
        String id,
        String name,
        @JsonProperty("country_name") String countryName,
        @JsonProperty("country_code") String countryCode,
        @JsonProperty("administrative_division_level_1_name") String provinceName,
        @JsonProperty("administrative_division_level_1_type") String provinceType,
        @JsonProperty("administrative_division_level_2_name") String cityName,
        @JsonProperty("administrative_division_level_2_type") String cityType,
        @JsonProperty("administrative_division_level_3_name") String districtName,
        @JsonProperty("administrative_division_level_3_type") String districtType,
        @JsonProperty("postal_code") String postalCode
) {
}
