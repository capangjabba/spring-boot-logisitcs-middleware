package com.example.logisticsmiddleware.services;

import com.example.logisticsmiddleware.couriers.CityLinkClient;
import com.example.logisticsmiddleware.dto.request.RateRequestDto;
import com.example.logisticsmiddleware.dto.response.CourierRateResponse;
import com.example.logisticsmiddleware.dto.response.RatesApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateService {

    // Temporary testing method – replace later with real courier aggregation
    public RatesApiResponse getRates(RateRequestDto dto) {
        // TODO: Real implementation - call each courier client, collect rates, handle errors/failures

        // Fake data for testing (matches your example)
        List<CourierRateResponse> rates = new ArrayList<>();
//        CourierRateResponse citylink = new CityLinkClient();
//        CourierRateResponse citylink = new CourierRateResponse();
//        citylink.setCourier("Citylink");
//        citylink.setRate(new BigDecimal("10"));
//        rates.add(citylink);
//
//        CourierRateResponse jnt = new CourierRateResponse();
//        jnt.setCourier("J&T");
//        jnt.setRate(new BigDecimal("20"));
//        rates.add(jnt);

        // rates.sort(Comparator.comparing(CourierRateResponse::getRate));

        RatesApiResponse response = new RatesApiResponse();
        response.setData(rates);
        return response;
    }
    // Future real method signature
    // public List<CourierRateResponse> getRates(RateRequestDto dto) { ... }
}