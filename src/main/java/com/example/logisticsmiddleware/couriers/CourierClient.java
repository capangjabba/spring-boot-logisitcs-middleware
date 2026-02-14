package com.example.logisticsmiddleware.couriers;

import com.example.logisticsmiddleware.dto.request.RateRequestDto;
import com.example.logisticsmiddleware.dto.response.CourierRateResponse;
import reactor.core.publisher.Mono;

public interface CourierClient {
    Mono<CourierRateResponse> getRates(RateRequestDto request);
}
