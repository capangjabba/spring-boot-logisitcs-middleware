package com.example.logisticsmiddleware.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class RatesApiResponse {

    private List<CourierRateResponse> data;
}