package com.custard.journal_service.adapter.dto;

import com.custard.journal_service.infrastructure.clients.dto.ApiResponse;
import jakarta.annotation.PostConstruct;

public class FailureApiResponse<String> extends ApiResponse<String> {
    @PostConstruct
    public void setCode() {
        super.setCode("01");
    }
}
