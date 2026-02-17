package com.example.logisticsmiddleware.services;

import com.example.logisticsmiddleware.couriers.citylink.CityLinkClient;
import com.example.logisticsmiddleware.couriers.jnt.JnTClient;
import com.example.logisticsmiddleware.dto.request.RateRequestDto;
import com.example.logisticsmiddleware.dto.response.CourierRateResponse;
import com.example.logisticsmiddleware.dto.response.RatesApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateService {

    private final CityLinkClient cityLinkClient;
    private final JnTClient jnTClient;

    // Temporary testing method – replace later with real courier aggregation
    public RatesApiResponse getRates(RateRequestDto dto) {
        long start = System.nanoTime();

        Flux<CourierRateResponse> ratesFlux = Flux.merge(
                cityLinkClient.getRate(dto),
                jnTClient.getRate(dto)
                // add more: posLajuClient.getRate(dto), etc.
        );

        // Collect, sort, block once
        List<CourierRateResponse> rates = ratesFlux
                .filter(r -> r.getRate() != null)
                .collectList()
                .block()  // only block here (fine in MVC)
                .stream()
                .sorted(Comparator.comparing(CourierRateResponse::getRate))
                .toList();
        long ms = (System.nanoTime() - start) / 1_000_000;
        BigDecimal best = rates.isEmpty() ? null : rates.get(0).getRate();
        log.info("Rates done: returned={} bestRate={} durationMs={}", rates.size(), best, ms);

        RatesApiResponse response = new RatesApiResponse();
        response.setData(rates);
        return response;
    }
}