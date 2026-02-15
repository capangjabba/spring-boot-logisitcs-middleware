package com.example.logisticsmiddleware.couriers.jnt.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Component
public class JntRateHtmlParser {

    public Optional<BigDecimal> parcelShippingRate(String html) {
        return shippingRateFor(html, "Parcel");
    }

    public Optional<BigDecimal> shippingRateFor(String html, String goodsType) {
        if (html == null || html.isBlank()) return Optional.empty();

        Document doc = Jsoup.parseBodyFragment(html);

        // 1) Mobile table: rows like
        // TH = "Shipping Rates", TD[0]=Parcel, TD[1]=Document
        Optional<Integer> idx = findMobileGoodsTypeIndex(doc, goodsType);
        if (idx.isPresent()) {
            Optional<BigDecimal> mobile = findMobileValue(doc, "Shipping Rates", idx.get());
            if (mobile.isPresent()) return mobile;
        }

        // 2) Desktop table: row like
        // TD[0]=Parcel, TD[1]=6.99, TD[2]=0.00, TD[3]=6.99
        Optional<BigDecimal> desktop = findDesktopParcelShippingRate(doc, goodsType);
        if (desktop.isPresent()) return desktop;

        return Optional.empty();
    }

    private Optional<Integer> findMobileGoodsTypeIndex(Document doc, String goodsType) {
        Elements rows = doc.select("table tbody tr");
        for (Element row : rows) {
            Element th = row.selectFirst("th");
            if (th == null) continue;

            if (normalize(th.text()).equalsIgnoreCase("goods type")) {
                Elements tds = row.select("td");
                for (int i = 0; i < tds.size(); i++) {
                    if (normalize(tds.get(i).text()).equalsIgnoreCase(normalize(goodsType))) {
                        return Optional.of(i);
                    }
                }
            }
        }
        return Optional.empty();
    }

    private Optional<BigDecimal> findMobileValue(Document doc, String label, int idx) {
        Elements rows = doc.select("table tbody tr");
        for (Element row : rows) {
            Element th = row.selectFirst("th");
            if (th == null) continue;

            if (normalize(th.text()).equalsIgnoreCase(normalize(label))) {
                Elements tds = row.select("td");
                if (idx >= 0 && idx < tds.size()) {
                    return parseMoney(tds.get(idx).text());
                }
            }
        }
        return Optional.empty();
    }

    private Optional<BigDecimal> findDesktopParcelShippingRate(Document doc, String goodsType) {
        Elements rows = doc.select("table tbody tr");
        for (Element row : rows) {
            Elements tds = row.select("td");
            if (tds.size() < 2) continue;

            String firstCell = normalize(tds.get(0).text());
            if (firstCell.equalsIgnoreCase(normalize(goodsType))) {
                // shipping rates is 2nd column in desktop table
                return parseMoney(tds.get(1).text());
            }
        }
        return Optional.empty();
    }

    private Optional<BigDecimal> parseMoney(String raw) {
        if (raw == null) return Optional.empty();
        String s = raw.trim();
        if (s.isEmpty() || s.equalsIgnoreCase("N/A")) return Optional.empty();

        s = s.replace(",", "").replaceAll("[^0-9.\\-]", "");
        if (s.isEmpty()) return Optional.empty();

        try {
            return Optional.of(new BigDecimal(s));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private String normalize(String s) {
        return s == null ? "" : s.replaceAll("\\s+", " ").trim();
    }
}
