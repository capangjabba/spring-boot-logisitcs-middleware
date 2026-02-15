package com.example.logisticsmiddleware.couriers.citylink;

import com.example.logisticsmiddleware.couriers.CourierClient;
import com.example.logisticsmiddleware.dto.request.RateRequestDto;
import com.example.logisticsmiddleware.dto.response.CourierRateResponse;
import com.example.logisticsmiddleware.utils.PostcodeStateMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class CityLinkClient implements CourierClient {
    @Getter
    private static final String courierCode = "CityLink";

    private final WebClient webClient;
    private final PostcodeStateMapper stateMapper;

    @Override
    public Mono<CourierRateResponse> getRate(RateRequestDto request) {
        log.debug("RateRequestDTO: {}", request);

        String originState = stateMapper.getStateFromPostcode(request.getOriginPostcode());
        String destState = stateMapper.getStateFromPostcode(request.getDestinationPostcode());
        if (originState == null || destState == null) {
            return Mono.empty();
        }

        CityLinkRequest body = new CityLinkRequest();
        body.setOrigin_state(originState);
        body.setOrigin_postcode(request.getOriginPostcode());
        body.setDestination_state(destState);
        if (!originState.equals(destState)){
            body.setDestination_postcode("50000");
        }else {
            body.setDestination_postcode(request.getDestinationPostcode());
        }

        body.setLength(request.getLengthCm().toPlainString());
        body.setWidth(request.getWidthCm().toPlainString());
        body.setHeight(request.getHeightCm().toPlainString());

        body.setParcel_weight(request.getWeightKg().toPlainString());

        log.debug("Body : {}",body);

        String REQUEST_URL = "https://www.citylinkexpress.com/wp-json/wp/v2/getShippingRate";
        return webClient.post()
                .uri(REQUEST_URL)
                .bodyValue(body)
                .exchangeToMono(clientResponse -> {
                    HttpStatusCode status = clientResponse.statusCode();
                    log.info("<<< CityLink RESPONSE STATUS: {}", status);

                    if (status.isError()) {
                        return clientResponse.bodyToMono(String.class)
                                .doOnNext(errorBody -> log.error("<<< ERROR RESPONSE BODY:\n{}", errorBody))
                                .then(Mono.empty());
                    }

                    return clientResponse.bodyToMono(CityLinkResponse.class)
                            .doOnNext(resp -> log.info("<<< Parsed CityLinkResponse object: {}", resp))
                            .flatMap(resp -> {
                                if (resp != null && resp.getReq() != null && resp.getReq().getData() != null) {
                                    Data data = resp.getReq().getData();

                                    if ("00".equals(data.getCode()) && data.getRate() != null && data.getMessage() != null && data.getMessage().isEmpty()) {
                                        log.info("Valid rate extracted: {} (estimated final days: {}, date: {})",
                                                data.getRate(), data.getFinal_days(), data.getDayString());

                                        return Mono.just(CourierRateResponse.builder()
                                                .courier(getCourierCode())
                                                .rate(data.getRate())
                                                .build());
                                    } else {
                                        log.error("Invalid/success=false response: code={}, message='{}'", data.getCode(), data.getMessage());
                                    }
                                } else {
                                    log.error("Malformed response structure: {}", resp);
                                }
                                return Mono.empty();
                            });
                })
                .onErrorResume(throwable -> {
                    log.error("CityLink request failed entirely: {}", throwable.toString(), throwable);
                    return Mono.empty();
                });
    }
}
