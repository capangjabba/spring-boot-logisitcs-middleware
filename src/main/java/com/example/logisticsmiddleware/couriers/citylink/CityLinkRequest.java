package com.example.logisticsmiddleware.couriers.citylink;

import lombok.Data;

@Data
public class CityLinkRequest {
    private String origin_country = "MY";
    private String origin_state;     // can be resolved from postcode
    private String origin_postcode;
    private String destination_country = "MY";
    private String destination_state; // can be resolved from postcode aswell
    private String destination_postcode;
    private String length;
    private String width;
    private String height;
    private String selected_type = "1"; // assume parcel
    private String parcel_weight;
    private String document_weight = ""; // fixed empty
}