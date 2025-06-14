package com.example.coupon_service.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CodeGenerationConfig {

    @Builder.Default
    private String characterSet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private int length;

    private String prefix;

    private String suffix;
    
}