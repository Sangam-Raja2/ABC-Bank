package com.sangam.abcbank.emiservice.controller;

import com.sangam.abcbank.emiservice.dto.EmiCalculatorRequest;
import com.sangam.abcbank.emiservice.dto.EmiCalculatorResponse;
import com.sangam.abcbank.emiservice.service.EmiCalculatorService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/emi/calculator")
public class EmiCalculatorController {

    private final EmiCalculatorService calculatorService;

    public EmiCalculatorController(EmiCalculatorService calculatorService) {
        this.calculatorService = calculatorService;
    }

    /** POST /api/emi/calculator - returns EMI amount + full amortization schedule. Open endpoint. */
    @PostMapping
    public EmiCalculatorResponse calculate(@Valid @RequestBody EmiCalculatorRequest request) {
        return calculatorService.calculate(request);
    }
}
