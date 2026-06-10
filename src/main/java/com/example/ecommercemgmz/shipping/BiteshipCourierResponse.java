package com.example.ecommercemgmz.shipping;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BiteshipCourierResponse(
        @JsonProperty("available_for_cash_on_delivery") boolean availableForCashOnDelivery,
        @JsonProperty("available_for_proof_of_delivery") boolean availableForProofOfDelivery,
        @JsonProperty("available_for_instant_waybill_id") boolean availableForInstantWaybillId,
        @JsonProperty("courier_name") String courierName,
        @JsonProperty("courier_code") String courierCode,
        @JsonProperty("courier_service_name") String courierServiceName,
        @JsonProperty("courier_service_code") String courierServiceCode,
        String tier,
        String description,
        @JsonProperty("service_type") String serviceType,
        @JsonProperty("shipping_type") String shippingType,
        @JsonProperty("shipment_duration_range") String shipmentDurationRange,
        @JsonProperty("shipment_duration_unit") String shipmentDurationUnit
) {
}
