package com.example.logisticsmiddleware.couriers.citylink;

import lombok.*;
import java.math.BigDecimal;

@lombok.Data
class Data {
    private BigDecimal rate;      // use BigDecimal for precision/safety
    private String code;          // "00" = success
    private Integer api_days;
    private Integer final_days;
    private String dayString;     // e.g., "17-02-2026"
    private Integer weekendDays;
    private String message;
}

@lombok.Data
public class CityLinkResponse {
    private Req req;
}

@lombok.Data
class Req {
    private Data data;
    private int status;  // 200
    // headers ignored for now
}
