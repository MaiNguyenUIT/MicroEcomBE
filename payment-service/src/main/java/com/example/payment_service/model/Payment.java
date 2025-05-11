package com.example.payment_service.model;

import com.example.payment_service.ENUM.PAYMENT_STATUS;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Document(
        "payments"
)
public class Payment {
    @Id
    private String id;
    private String orderId;
    private String amount;
    private PAYMENT_STATUS paymentStatus;
    private String method;
    private LocalDateTime paymentDate = LocalDateTime.now();
}
