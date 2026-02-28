package com.example.logisticsmiddleware.config;

import com.example.logisticsmiddleware.dto.request.RateRequestDto;
import org.junit.jupiter.api.Test;
import org.springframework.cache.interceptor.KeyGenerator;

import java.lang.reflect.Method;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CacheConfigTest {

    private final CacheConfig cacheConfig = new CacheConfig();

    @Test
    void rateRequestCacheKeyGeneratorShouldNormalizeCaseWhitespaceAndNumberScale() throws NoSuchMethodException {
        KeyGenerator keyGenerator = cacheConfig.rateRequestCacheKeyGenerator();
        Method method = SampleTarget.class.getDeclaredMethod("load", RateRequestDto.class);

        Object firstKey = keyGenerator.generate(new SampleTarget(), method, request(
                " my ",
                " 13200 ",
                " sg ",
                " 018989 ",
                "1.00",
                "10.0",
                "10.00",
                "5.000"
        ));
        Object secondKey = keyGenerator.generate(new SampleTarget(), method, request(
                "MY",
                "13200",
                "SG",
                "018989",
                "1",
                "10",
                "10",
                "5"
        ));

        assertThat(firstKey).isEqualTo(secondKey);
        assertThat(firstKey).hasToString("MY:13200:SG:018989:1:10:10:5");
    }

    private static RateRequestDto request(
            String originCountryCode,
            String originPostcode,
            String destinationCountryCode,
            String destinationPostcode,
            String weightKg,
            String lengthCm,
            String widthCm,
            String heightCm
    ) {
        RateRequestDto dto = new RateRequestDto();
        dto.setOriginCountryCode(originCountryCode);
        dto.setOriginPostcode(originPostcode);
        dto.setDestinationCountryCode(destinationCountryCode);
        dto.setDestinationPostcode(destinationPostcode);
        dto.setWeightKg(new BigDecimal(weightKg));
        dto.setLengthCm(new BigDecimal(lengthCm));
        dto.setWidthCm(new BigDecimal(widthCm));
        dto.setHeightCm(new BigDecimal(heightCm));
        return dto;
    }

    private static final class SampleTarget {
        @SuppressWarnings("unused")
        void load(RateRequestDto dto) {
        }
    }
}
