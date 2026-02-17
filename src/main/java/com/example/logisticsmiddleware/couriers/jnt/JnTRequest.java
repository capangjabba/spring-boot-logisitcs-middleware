package com.example.logisticsmiddleware.couriers.jnt;

import lombok.Data;

@Data
public class JnTRequest {
    private String _token = ""; // fixed???
    private String shipping_rates_type = "domestic";
    private String sender_postcode;
    private String receiver_postcode;
    private String destination_country = "MY"; // force domestic; use provided for international
    private String shipping_type = "EZ"; // will make configurable later
    private String weight;
    private String length;
    private String width;
    private String height;
    private String item_value = "";
}


