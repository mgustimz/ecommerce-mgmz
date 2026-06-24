package com.example.ecommercemgmz.shipping;

import com.example.ecommercemgmz.address.CustomerAddress;
import com.example.ecommercemgmz.cart.CartItem;
import com.example.ecommercemgmz.common.ApiException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Service
public class ShippingService {
    private final BigDecimal flatFee;
    private final BigDecimal freeShippingThreshold;
    private final String externalUrl;
    private final String externalApiKey;
    private final String biteshipBaseUrl;
    private final String biteshipApiKey;
    private final String originAreaId;
    private final String originPostalCode;
    private final String originLatitude;
    private final String originLongitude;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public ShippingService(ObjectMapper objectMapper,
                           @Value("${app.shipping.flat-fee}") BigDecimal flatFee,
                           @Value("${app.shipping.free-shipping-threshold}") BigDecimal freeShippingThreshold,
                           @Value("${app.shipping.external-url:}") String externalUrl,
                           @Value("${app.shipping.external-api-key:}") String externalApiKey,
                           @Value("${app.shipping.biteship-base-url}") String biteshipBaseUrl,
                           @Value("${app.shipping.biteship-api-key:}") String biteshipApiKey,
                           @Value("${app.shipping.origin-area-id:}") String originAreaId,
                           @Value("${app.shipping.origin-postal-code:}") String originPostalCode,
                           @Value("${app.shipping.origin-latitude:}") String originLatitude,
                           @Value("${app.shipping.origin-longitude:}") String originLongitude) {
        this.objectMapper = objectMapper;
        this.flatFee = flatFee;
        this.freeShippingThreshold = freeShippingThreshold;
        this.externalUrl = externalUrl;
        this.externalApiKey = externalApiKey;
        this.biteshipBaseUrl = biteshipBaseUrl;
        this.biteshipApiKey = biteshipApiKey;
        this.originAreaId = originAreaId;
        this.originPostalCode = originPostalCode;
        this.originLatitude = originLatitude;
        this.originLongitude = originLongitude;
        this.restClient = RestClient.create();
    }

    public BiteshipAreasResponse searchAreas(String query, String postalCode, String type) {
        if (!isBiteshipConfigured()) {
            String safePostalCode = postalCode == null ? "" : postalCode;
            String safeName = query == null || query.isBlank() ? "Local Area" : query;
            return new BiteshipAreasResponse(true, List.of(new BiteshipAreaResponse(
                    "local-area",
                    safeName,
                    "Indonesia",
                    "ID",
                    "Local Province",
                    "province",
                    safeName,
                    "city",
                    safeName,
                    "district",
                    safePostalCode
            )));
        }
        String input = StringUtils.hasText(query) ? query : postalCode;
        if (!StringUtils.hasText(input)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Query or postalCode is required");
        }
        return restClient.get()
                .uri(biteshipBaseUrl + "/v1/maps/areas?countries=ID&input={input}&type={type}", input, StringUtils.hasText(type) ? type : "single")
                .header("authorization", biteshipApiKey)
                .retrieve()
                .body(BiteshipAreasResponse.class);
    }

    public BiteshipCouriersResponse getCouriers() {
        if (!isBiteshipConfigured()) {
            return new BiteshipCouriersResponse(true, "courier", List.of(
                    new BiteshipCourierResponse(
                            false,
                            false,
                            true,
                            "Local Courier",
                            "local",
                            "Regular",
                            "REG",
                            "standard",
                            "Local regular parcel delivery",
                            "regular",
                            "parcel",
                            "2 - 4",
                            "days"
                    ),
                    new BiteshipCourierResponse(
                            false,
                            false,
                            true,
                            "Local Courier",
                            "local",
                            "Express",
                            "EXP",
                            "premium",
                            "Local express parcel delivery",
                            "express",
                            "parcel",
                            "1 - 2",
                            "days"
                    )
            ));
        }
        return restClient.get()
                .uri(biteshipBaseUrl + "/v1/couriers")
                .header("authorization", biteshipApiKey)
                .retrieve()
                .body(BiteshipCouriersResponse.class);
    }

    public List<ShippingRateResponse> getRates(CustomerAddress address, List<CartItem> cartItems, BigDecimal subtotal) {
        return getRates(address, cartItems, subtotal, null);
    }

    public List<ShippingRateResponse> getRates(CustomerAddress address, List<CartItem> cartItems, BigDecimal subtotal, ShippingRateRequest request) {
        int totalWeightGram = cartItems.stream()
                .mapToInt(item -> item.getProduct().getWeightGram() * item.getQuantity())
                .sum();
        if (isBiteshipConfigured()) {
            try {
                List<ShippingRateResponse> rates = getBiteshipRates(address, cartItems, request);
                if (!rates.isEmpty()) {
                    return rates;
                }
            } catch (ApiException exception) {
                throw exception;
            } catch (RuntimeException ignored) {
                // Fall back to local rates so checkout remains available if Biteship is down.
            }
        }
        if (StringUtils.hasText(externalUrl)) {
            try {
                ShippingRateResponse[] rates = restClient.post()
                        .uri(externalUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-Key", externalApiKey)
                        .body(new ExternalShippingRateRequest(address.getCity(), address.getProvince(), address.getPostalCode(), totalWeightGram, subtotal))
                        .retrieve()
                        .body(ShippingRateResponse[].class);
                if (rates != null && rates.length > 0) {
                    return List.of(rates);
                }
            } catch (RuntimeException ignored) {
                // Fall back to local rates so checkout remains available if the courier API is down.
            }
        }
        return localRates(subtotal, totalWeightGram);
    }

    public ShippingRateResponse findRate(String serviceCode, CustomerAddress address, List<CartItem> cartItems, BigDecimal subtotal) {
        return getRates(address, cartItems, subtotal).stream()
                .filter(rate -> rate.serviceCode().equalsIgnoreCase(serviceCode))
                .findFirst()
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Shipping service is not available"));
    }

    private List<ShippingRateResponse> localRates(BigDecimal subtotal, int totalWeightGram) {
        BigDecimal regularFee = calculateLocalFee(subtotal, totalWeightGram);
        BigDecimal expressFee = regularFee.add(BigDecimal.valueOf(10000));
        return List.of(
                new ShippingRateResponse("REG", "LOCAL", "Regular", regularFee, "2-4 days"),
                new ShippingRateResponse("EXP", "LOCAL", "Express", expressFee, "1-2 days")
        );
    }

    private BigDecimal calculateLocalFee(BigDecimal subtotal, int totalWeightGram) {
        if (subtotal.compareTo(freeShippingThreshold) >= 0) {
            return BigDecimal.ZERO;
        }
        int extraKg = Math.max(0, (int) Math.ceil(totalWeightGram / 1000.0) - 1);
        return flatFee.add(BigDecimal.valueOf(extraKg).multiply(BigDecimal.valueOf(5000)));
    }

    private List<ShippingRateResponse> getBiteshipRates(CustomerAddress address, List<CartItem> cartItems, ShippingRateRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        if (StringUtils.hasText(originAreaId)) {
            body.put("origin_area_id", originAreaId);
        }
        if (StringUtils.hasText(originPostalCode)) {
            body.put("origin_postal_code", originPostalCode);
        }
        if (StringUtils.hasText(originLatitude) && StringUtils.hasText(originLongitude)) {
            body.put("origin_latitude", new BigDecimal(originLatitude));
            body.put("origin_longitude", new BigDecimal(originLongitude));
        }
        if (StringUtils.hasText(address.getAreaId())) {
            body.put("destination_area_id", address.getAreaId());
        }
        if (StringUtils.hasText(address.getPostalCode())) {
            body.put("destination_postal_code", address.getPostalCode());
        }
        if (address.getLatitude() != null && address.getLongitude() != null) {
            body.put("destination_latitude", address.getLatitude());
            body.put("destination_longitude", address.getLongitude());
        }
        if (request != null && StringUtils.hasText(request.type())) {
            body.put("type", request.type());
        }
        List<String> courierCodes = request == null ? List.of() : request.courierCodes();
        List<String> selectedCouriers = courierCodes == null || courierCodes.isEmpty() ? List.of("jne", "sicepat", "anteraja", "jnt") : courierCodes;
        body.put("couriers", String.join(",", selectedCouriers));
        body.put("items", cartItems.stream()
                .map(item -> Map.of(
                        "name", item.getProduct().getName(),
                        "description", item.getProduct().getSku(),
                        "category", item.getProduct().getShippingCategory().name(),
                        "sku", item.getProduct().getSku(),
                        "value", item.getProduct().getPrice(),
                        "length", item.getProduct().getLengthCm(),
                        "width", item.getProduct().getWidthCm(),
                        "height", item.getProduct().getHeightCm(),
                        "weight", Math.max(item.getProduct().getWeightGram(), 1),
                        "quantity", item.getQuantity()
                ))
                .toList());
        if (request != null && request.courierInsurance() != null) {
            body.put("courier_insurance", request.courierInsurance());
        }
        if (request != null && request.destinationCashOnDelivery() != null) {
            body.put("destination_cash_on_delivery", request.destinationCashOnDelivery());
            body.put("destination_cash_on_delivery_type", request.destinationCashOnDeliveryType());
        }

        try {
            Object response = restClient.post()
                    .uri(biteshipBaseUrl + "/v1/rates/couriers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("authorization", biteshipApiKey)
                    .body(body)
                    .retrieve()
                    .body(Object.class);
            return normalizeBiteshipRates(response);
        } catch (RestClientResponseException exception) {
            throw mapBiteshipRatesError(exception);
        }
    }

    private ApiException mapBiteshipRatesError(RestClientResponseException exception) {
        String code = extractBiteshipCode(exception.getResponseBodyAsString());
        if (code == null) {
            return new ApiException(
                    HttpStatus.BAD_GATEWAY,
                    "Shipping rate provider returned an error. Please try again later."
            );
        }
        return switch (code) {
            case "40001001" -> new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Postal code is invalid or not available for courier rate checking. Please choose another address or contact support.",
                    code
            );
            case "40001002" -> new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Shipping rate request is missing required data. Please complete origin, destination, courier, and item details.",
                    code
            );
            case "40001010" -> new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "No courier is available for this route. Please choose another address or enable more courier options.",
                    code
            );
            default -> new ApiException(
                    HttpStatus.BAD_GATEWAY,
                    "Shipping rate provider returned an error. Please try again later.",
                    code
            );
        };
    }

    private String extractBiteshipCode(String responseBody) {
        if (!StringUtils.hasText(responseBody)) {
            return null;
        }
        try {
            Map<String, Object> body = objectMapper.readValue(responseBody, new TypeReference<>() {
            });
            Object code = body.get("code");
            if (code == null) {
                code = body.get("error_code");
            }
            return code == null ? null : code.toString();
        } catch (Exception ignored) {
            return null;
        }
    }

    private List<ShippingRateResponse> normalizeBiteshipRates(Object response) {
        if (!(response instanceof Map<?, ?> map)) {
            return List.of();
        }
        Object pricing = map.get("pricing");
        if (!(pricing instanceof List<?> pricingList)) {
            return List.of();
        }
        List<ShippingRateResponse> rates = new ArrayList<>();
        for (Object item : pricingList) {
            if (item instanceof Map<?, ?> rate) {
                String courierCode = stringValue(rate.get("courier_code"));
                String serviceCode = stringValue(rate.get("courier_service_code"));
                BigDecimal price = decimalValue(rate.get("price"));
                rates.add(new ShippingRateResponse(
                        courierCode + ":" + serviceCode,
                        stringValue(rate.get("courier_name")),
                        stringValue(rate.get("courier_service_name")),
                        price,
                        stringValue(rate.get("duration"))
                ));
            }
        }
        return rates;
    }

    private boolean isBiteshipConfigured() {
        return StringUtils.hasText(biteshipApiKey)
                && (StringUtils.hasText(originAreaId)
                || StringUtils.hasText(originPostalCode)
                || (StringUtils.hasText(originLatitude) && StringUtils.hasText(originLongitude)));
    }

    private String stringValue(Object value) {
        return value == null ? "" : value.toString();
    }

    private BigDecimal decimalValue(Object value) {
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        if (value != null && StringUtils.hasText(value.toString())) {
            return new BigDecimal(value.toString());
        }
        return BigDecimal.ZERO;
    }
}
