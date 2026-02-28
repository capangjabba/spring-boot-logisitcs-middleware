package com.example.logisticsmiddleware.config;

import com.example.logisticsmiddleware.dto.request.RateRequestDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String SHIPPING_RATES_CACHE = "shippingRates";

    @Bean
    public CacheManager cacheManager(
            RedisConnectionFactory redisConnectionFactory,
            @Value("${app.cache.rates.ttl}") Duration ratesTtl
    ) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ratesTtl)
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new GenericJackson2JsonRedisSerializer()
                ));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfig)
                .withCacheConfiguration(SHIPPING_RATES_CACHE, defaultConfig)
                .build();
    }

    @Bean("rateRequestCacheKeyGenerator")
    public KeyGenerator rateRequestCacheKeyGenerator() {
        return new KeyGenerator() {
            @Override
            public Object generate(Object target, Method method, Object... params) {
                if (params.length == 1 && params[0] instanceof RateRequestDto dto) {
                    return String.join(":",
                            normalize(dto.getOriginCountryCode()),
                            normalize(dto.getOriginPostcode()),
                            normalize(dto.getDestinationCountryCode()),
                            normalize(dto.getDestinationPostcode()),
                            normalize(dto.getWeightKg()),
                            normalize(dto.getLengthCm()),
                            normalize(dto.getWidthCm()),
                            normalize(dto.getHeightCm())
                    );
                }

                return method.getName();
            }
        };
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }

    private static String normalize(BigDecimal value) {
        return value == null ? "" : value.stripTrailingZeros().toPlainString();
    }
}
