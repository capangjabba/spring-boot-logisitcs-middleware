package com.example.logisticsmiddleware.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CourierRateResponse {

    private String courier;

    private BigDecimal rate;
}