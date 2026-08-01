package com.sangam.abcbank.loanservice.client;

import com.sangam.abcbank.common.dto.KycResponseDto;
import com.sangam.abcbank.common.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "banking-service", url = "${banking-service.url}",
        configuration = FeignClientConfig.class)
public interface BankingServiceClient {

    @GetMapping("/api/kyc/customer/{customerId}")
    KycResponseDto getCurrentCustomerDetails(String customerId);
}
