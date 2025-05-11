package com.example.payment_service.exception;

import lombok.Data;

@Data
public class ErrorDetail {
    private String code;
    private String message;
}
