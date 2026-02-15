package com.example.logisticsmiddleware.couriers.jnt;

import com.example.logisticsmiddleware.couriers.CourierClient;
import com.example.logisticsmiddleware.couriers.jnt.parser.JntRateHtmlParser;
import com.example.logisticsmiddleware.dto.request.RateRequestDto;
import com.example.logisticsmiddleware.dto.response.CourierRateResponse;
import com.example.logisticsmiddleware.utils.CsrfSessionClient;
import com.example.logisticsmiddleware.utils.PostcodeStateMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class JnTClient implements CourierClient {
    @Getter
    private final String courierCode = "J&T";

    private final String TOKEN_PAGE_URL = "https://www.jtexpress.my/shipping-rates";
    private static final String RATE_POST_URL  = "https://www.jtexpress.my/shipping-rates";

    private final PostcodeStateMapper stateMapper;
    private final WebClient webClient;
    private final CsrfSessionClient csrfSessionClient;
    private final JntRateHtmlParser parser;
    @Override
    public Mono<CourierRateResponse> getRate(RateRequestDto request) {
        return csrfSessionClient.startSession(TOKEN_PAGE_URL)
                .doOnNext(session -> {log.info("Session: {}", session);})
                .flatMap(session -> {
                    JnTRequest jntReq = toJnTRequest(request);
                    jntReq.set_token(session.token());

                    return webClient.post()
                            .uri(RATE_POST_URL) // <-- MUST be the real JSON endpoint
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_HTML)
                            .header(HttpHeaders.COOKIE, session.cookieHeader())
                            .header(HttpHeaders.REFERER, TOKEN_PAGE_URL)
                            .header(HttpHeaders.ORIGIN, "https://www.jtexpress.my")
                            .header("X-Requested-With", "XMLHttpRequest")
                            .header("X-CSRF-TOKEN", session.token()) // optional but often required
                            .bodyValue(jntReq)
                            .exchangeToMono(resp -> {
                                var headers = resp.headers().asHttpHeaders();
                                var ct = headers.getContentType();
                                var loc = headers.getFirst(HttpHeaders.LOCATION);

                                if (resp.statusCode().is3xxRedirection()) {
                                    return resp.bodyToMono(String.class).defaultIfEmpty("")
                                            .flatMap(body -> Mono.error(new IllegalStateException(
                                                    "J&T returned redirect: " + resp.statusCode()
                                                            + " location=" + loc
                                                            + " contentType=" + ct
                                                            + " bodySnippet=" + body
                                            )));
                                }

                                // JSON path
                                if (ct != null && ct.isCompatibleWith(MediaType.APPLICATION_JSON)) {
                                    return resp.bodyToMono(CourierRateResponse.class);
                                }

                                // HTML path (or anything else): parse it
                                return resp.bodyToMono(String.class).defaultIfEmpty("")
                                        .flatMap(html -> {
                                            var rateOpt = parser.parcelShippingRate(html);
                                            if (rateOpt.isEmpty()) {
                                                return Mono.error(new IllegalStateException(
                                                        "Could not parse Parcel shipping rate from HTML. contentType=" + ct
                                                ));
                                            }

                                            var rate = rateOpt.get();

                                            log.info("Parsed J&T Parcel Shipping Rate = {}", rate);
                                            CourierRateResponse out = new CourierRateResponse();
                                            // adjust to your DTO fields:
                                            // out.setCourierCode(courierCode);
                                            // out.setShippingRate(rateOpt.get());

                                            return Mono.just(CourierRateResponse.builder()
                                                    .courier(getCourierCode())
                                                    .rate(rate)
                                                    .build());
                                        });
                            });
                });
    }

    private JnTRequest toJnTRequest(RateRequestDto request) {
        String originState = stateMapper.getStateFromPostcode(request.getOriginPostcode());
        JnTRequest r = new JnTRequest();
        if (!request.getDestinationCountryCode().equals("MY")){
            r.setShipping_type("International");
        }
        r.setSender_postcode(request.getOriginPostcode());
        r.setReceiver_postcode(request.getDestinationPostcode());
        r.setWeight(String.valueOf(request.getWeightKg()));
        r.setLength(String.valueOf(request.getLengthCm()));
        r.setWidth(String.valueOf(request.getWidthCm()));
        r.setHeight(String.valueOf(request.getHeightCm()));

        return r;
    }


}
