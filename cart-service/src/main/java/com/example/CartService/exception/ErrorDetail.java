package com.example.CartService.exception;

import lombok.Data;

@Data
public class ErrorDetail {
    private String code;
    private String message;
}
