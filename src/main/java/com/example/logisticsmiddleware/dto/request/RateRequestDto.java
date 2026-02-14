package com.example.logisticsmiddleware.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RateRequestDto {
    @NotBlank(message = "originCountryCode is required")
    @Pattern(regexp = "^[A-Z]{2}$", message = "originCountryCode must be ISO-3166 alpha-2 (e.g. MY)")
    private String originCountryCode;

    @NotBlank(message = "originPostcode is required")
    private String originPostcode;

    @NotBlank(message = "destinationCountryCode is required")
    @Pattern(regexp = "^[A-Z]{2}$", message = "destinationCountryCode must be ISO-3166 alpha-2 (e.g. MY)")
    private String destinationCountryCode;

    @NotBlank(message = "destinationPostcode is required")
    private String destinationPostcode;

    @NotNull(message = "weightKg is required")
    @Positive(message = "weightKg must be greater than zero")
    private BigDecimal weightKg;

    @PositiveOrZero(message = "lengthCm must be zero or positive")
    private BigDecimal lengthCm;

    @PositiveOrZero(message = "widthCm must be zero or positive")
    private BigDecimal widthCm;

    @PositiveOrZero(message = "heightCm must be zero or positive")
    private BigDecimal heightCm;
}
