package com.custard.journal_service.infrastructure.clients;

import com.custard.journal_service.infrastructure.clients.dto.SuccessApiResponse;
import com.custard.journal_service.infrastructure.clients.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("ACCOUNTS")
public interface AccountClient {

    @GetMapping(value = "/api/v1/account/get-account/{userId}")
    public ResponseEntity<SuccessApiResponse<UserDto>> getAccountDetails(@PathVariable("userId") String userId);


}
