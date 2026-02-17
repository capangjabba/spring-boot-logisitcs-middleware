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
        // TODO: Real implementation - call each courier client, collect rates, handle errors/failures

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

        RatesApiResponse response = new RatesApiResponse();
        response.setData(rates);
        return response;
    }
}