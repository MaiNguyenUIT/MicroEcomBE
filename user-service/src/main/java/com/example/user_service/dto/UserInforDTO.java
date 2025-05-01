package com.example.user_service.dto;

import com.example.user_service.model.Address;
import lombok.Data;
import org.springframework.boot.autoconfigure.amqp.RabbitConnectionDetails;

@Data
public class UserInforDTO {
    private String phone;
    private String photo;
    private Address address;
}
