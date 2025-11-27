package com.custard.journal_service.infrastructure.clients.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiResponse<T> {
    private String code = "00";
    private String message = "";

    private T data;
}
