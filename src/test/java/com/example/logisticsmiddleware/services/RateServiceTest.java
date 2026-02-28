package com.example.logisticsmiddleware.services;

import com.example.logisticsmiddleware.couriers.citylink.CityLinkClient;
import com.example.logisticsmiddleware.couriers.jnt.JnTClient;
import com.example.logisticsmiddleware.dto.request.RateRequestDto;
import com.example.logisticsmiddleware.dto.response.CourierRateResponse;
import com.example.logisticsmiddleware.dto.response.RatesApiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateServiceTest {

    @Mock
    private CityLinkClient cityLinkClient;

    @Mock
    private JnTClient jnTClient;

    @InjectMocks
    private RateService rateService;

    @Test
    void getRatesShouldSortResultsByLowestRate() {
        RateRequestDto dto = validRequest();
        when(cityLinkClient.getRate(dto)).thenReturn(Mono.just(rate("CityLink", "13.00")));
        when(jnTClient.getRate(dto)).thenReturn(Mono.just(rate("J&T", "6.99")));

        RatesApiResponse response = rateService.getRates(dto);

        assertThat(response.getData())
                .extracting(CourierRateResponse::getCourier)
                .containsExactly("J&T", "CityLink");
        assertThat(response.getData())
                .extracting(CourierRateResponse::getRate)
                .containsExactly(new BigDecimal("6.99"), new BigDecimal("13.00"));
    }

    @Test
    void getRatesShouldFilterOutResponsesWithoutRates() {
        RateRequestDto dto = validRequest();
        when(cityLinkClient.getRate(dto)).thenReturn(Mono.just(rate("CityLink", null)));
        when(jnTClient.getRate(dto)).thenReturn(Mono.just(rate("J&T", "6.99")));

        RatesApiResponse response = rateService.getRates(dto);

        assertThat(response.getData()).hasSize(1);
        assertThat(response.getData().get(0).getCourier()).isEqualTo("J&T");
        assertThat(response.getData().get(0).getRate()).isEqualByComparingTo("6.99");
    }

    private static RateRequestDto validRequest() {
        RateRequestDto dto = new RateRequestDto();
        dto.setOriginCountryCode("MY");
        dto.setOriginPostcode("13200");
        dto.setDestinationCountryCode("MY");
        dto.setDestinationPostcode("20596");
        dto.setWeightKg(new BigDecimal("2.0"));
        dto.setLengthCm(new BigDecimal("20"));
        dto.setWidthCm(new BigDecimal("15"));
        dto.setHeightCm(new BigDecimal("10"));
        return dto;
    }

    private static CourierRateResponse rate(String courier, String amount) {
        return CourierRateResponse.builder()
                .courier(courier)
                .rate(amount == null ? null : new BigDecimal(amount))
                .build();
    }
}
