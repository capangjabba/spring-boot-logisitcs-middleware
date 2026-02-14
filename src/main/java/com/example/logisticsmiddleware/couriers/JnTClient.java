package com.example.logisticsmiddleware.couriers;

import com.example.logisticsmiddleware.dto.request.RateRequestDto;
import com.example.logisticsmiddleware.dto.response.CourierRateResponse;
import reactor.core.publisher.Mono;

public class JnTClient implements CourierClient {
    private final String courierCode = "J&T";

    public String getCourierCode() {
        return this.courierCode;
    }

    @Override
    public Mono<CourierRateResponse> getRates(RateRequestDto request) {
        return null;
    }
}
