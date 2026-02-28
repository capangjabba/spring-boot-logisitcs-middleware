package com.example.logisticsmiddleware.couriers.jnt.parser;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class JntRateHtmlParserTest {

    private final JntRateHtmlParser parser = new JntRateHtmlParser();

    @Test
    void parcelShippingRateShouldReadMobileTableValue() {
        String html = """
                <table>
                  <tbody>
                    <tr><th>Goods Type</th><td>Parcel</td><td>Document</td></tr>
                    <tr><th>Shipping Rates</th><td>RM 6.99</td><td>RM 3.50</td></tr>
                  </tbody>
                </table>
                """;

        assertThat(parser.parcelShippingRate(html)).contains(new BigDecimal("6.99"));
    }

    @Test
    void shippingRateForShouldFallBackToDesktopTable() {
        String html = """
                <table>
                  <tbody>
                    <tr><td>Parcel</td><td>12.30</td><td>0.00</td><td>12.30</td></tr>
                    <tr><td>Document</td><td>3.00</td><td>0.00</td><td>3.00</td></tr>
                  </tbody>
                </table>
                """;

        assertThat(parser.shippingRateFor(html, "Parcel")).contains(new BigDecimal("12.30"));
    }

    @Test
    void shippingRateForShouldReturnEmptyWhenNoMatchingRateExists() {
        String html = """
                <table>
                  <tbody>
                    <tr><td>Document</td><td>N/A</td></tr>
                  </tbody>
                </table>
                """;

        assertThat(parser.shippingRateFor(html, "Parcel")).isEmpty();
    }
}
