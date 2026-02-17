package com.example.logisticsmiddleware.utils;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.HttpCookie;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class CsrfSessionClient {

    private final WebClient webClient;

    public CsrfSessionClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<SessionData> startSession(String pageUrl) {
        log.info("Starting J&T CSRF Session");
        return webClient.get()
                .uri(pageUrl)
                .accept(MediaType.TEXT_HTML) // override your default JSON accept
                .exchangeToMono(resp ->
                        resp.bodyToMono(String.class).map(html -> {
                            String token = extractTokenOrThrow(html);

                            List<String> setCookies = resp.headers()
                                    .asHttpHeaders()
                                    .get(HttpHeaders.SET_COOKIE);

                            String cookieHeader = toCookieHeader(setCookies);

                            return new SessionData(token, cookieHeader);
                        })
                );
    }

    private static String extractTokenOrThrow(String html) {
        Document doc = Jsoup.parse(html);
        Element input = doc.selectFirst("input[name=_token]");
        if (input == null) throw new IllegalStateException("_token input not found");
        String token = input.attr("value");
        if (token == null || token.isBlank()) throw new IllegalStateException("_token value is blank");
        return token;
    }

    // Convert Set-Cookie headers -> single Cookie header (name=value; name2=value2)
    private static String toCookieHeader(List<String> setCookies) {
        if (setCookies == null || setCookies.isEmpty()) return "";

        Map<String, String> cookieMap = new LinkedHashMap<>(); // keeps last value if same name repeats
        for (String setCookie : setCookies) {
            for (HttpCookie cookie : HttpCookie.parse(setCookie)) {
                cookieMap.put(cookie.getName(), cookie.getValue());
            }
        }

        return cookieMap.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("; "));
    }

    public record SessionData(String token, String cookieHeader) {}
}
