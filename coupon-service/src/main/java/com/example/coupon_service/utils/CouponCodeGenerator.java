package com.example.coupon_service.utils;

import com.example.coupon_service.model.CodeGenerationConfig;
import java.security.SecureRandom;
import java.util.Random;

public class CouponCodeGenerator {

    private static final Random random = new SecureRandom();

    public static String generateCode () {

        CodeGenerationConfig defaultConfig = CodeGenerationConfig.builder()
                .length(10)
                .prefix("COUPON-")
                .suffix("")
                .build();
        return generateCode(defaultConfig);

    }
    
    public static String generateCode (CodeGenerationConfig config) {

        if (config == null) {
            throw new IllegalArgumentException("Code generation configuration cannot be null");
        }

        StringBuilder code = new StringBuilder();

        appendPrefix(code, config.getPrefix());

        appendRandomChars(code, config.getLength(), config.getCharacterSet());

        appendSuffix(code, config.getSuffix());

        return code.toString();
    }

    private static void appendPrefix (StringBuilder builder, String prefix) {
        if (prefix != null && !prefix.isEmpty()) {
            builder.append(prefix);
        }
    }

    private static void appendRandomChars (StringBuilder builder, int length, String characterSet) {

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characterSet.length());
            builder.append(characterSet.charAt(index));
        }

    }

    private static void appendSuffix (StringBuilder builder, String suffix) {
        if (suffix != null && !suffix.isEmpty()) {
            builder.append(suffix);
        }
    }
}