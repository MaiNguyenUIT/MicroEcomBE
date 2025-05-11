package com.example.payment_service.repository;

import com.example.payment_service.model.Payment;
import org.bouncycastle.crypto.util.Pack;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PaymentRepository extends MongoRepository<Payment, String> {
}
