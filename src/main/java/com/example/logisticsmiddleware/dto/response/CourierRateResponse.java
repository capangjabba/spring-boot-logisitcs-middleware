package com.example.logisticsmiddleware.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourierRateResponse {

    private String courier;

    private BigDecimal rate;
}