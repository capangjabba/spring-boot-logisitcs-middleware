package com.example.logisticsmiddleware.controllers;

import com.example.logisticsmiddleware.dto.request.RateRequestDto;
import com.example.logisticsmiddleware.dto.response.RatesApiResponse;
import com.example.logisticsmiddleware.services.RateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/logistic")
public class RateController {

    private final RateService rateService;

    @GetMapping("/rates")
    public ResponseEntity<RatesApiResponse> getRates(@Valid @ModelAttribute RateRequestDto request) {
        // For testing only – later replace with proper List<CourierRateResponse>
        RatesApiResponse response = rateService.getRates(request);
        return ResponseEntity.ok(response);
    }
}