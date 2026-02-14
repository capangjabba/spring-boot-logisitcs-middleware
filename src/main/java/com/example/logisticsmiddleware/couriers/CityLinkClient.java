package com.example.logisticsmiddleware.couriers;

import com.example.logisticsmiddleware.dto.request.RateRequestDto;
import com.example.logisticsmiddleware.dto.response.CourierRateResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class CityLinkClient implements CourierClient{
    private static final String COURIER_CODE = "CityLink";
    private static final String REQUEST_URL = "https://www.citylinkexpress.com/wp-json/wp/v2/getShippingRate";

    private String getCourierCode() {
        return this.COURIER_CODE;
    }

    @Override
    public Mono<CourierRateResponse> getRates(RateRequestDto request) {
        CourierRateResponse rate = new CourierRateResponse();

        System.out.println("RateRequestDTO: "+request);


        return null;
    }
}
