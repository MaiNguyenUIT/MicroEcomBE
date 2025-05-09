package com.example.notification_service.service;

import com.example.notification_service.event.OrderConfirmEvent;
import com.example.notification_service.event.OrderStatusUpdateEvent;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

@Component
public class EmailService {
    @Value("${spring.mail.username}")
    private String fromEmail;

    @Autowired
    public  JavaMailSender mailSender;
    @Autowired
    private TemplateEngine templateEngine;

    @Bean
    public Consumer<OrderConfirmEvent> sendConfirmOrderMail(){
        return event -> {
            System.out.println("Send confirm mail");
            try {
                Context context = new Context();
                context.setVariable("orderId", event.getId());
                context.setVariable("orderAmount", event.getOrderAmount());
                context.setVariable("orderStatus", event.getOrderStatus());
                context.setVariable("userDisplayName", event.getUserDisplayName());

                String emailContent = templateEngine.process("order-confirmation", context);

                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());

                helper.setTo(event.getUserEmail());
                helper.setSubject("Xác nhận đơn hàng #" + event.getId());
                helper.setText(emailContent, true);
                helper.setFrom(fromEmail);

                mailSender.send(message);
            } catch (MessagingException e) {
                // Log lỗi hoặc xử lý theo yêu cầu nghiệp vụ
                throw new RuntimeException("Lỗi khi gửi email cập nhật trạng thái đơn hàng", e);
            }
        };
    }

    @Bean
    public Consumer<OrderStatusUpdateEvent> sendUpdateOrderMail(){
        return event -> {
            System.out.println("Send update mail");
            try {

                Context context = new Context();
                context.setVariable("orderId", event.getId());
                context.setVariable("orderStatus", event.getOrderStatus());
                context.setVariable("userDisplayName", event.getUserDisplayName());

                // Render template thành HTML
                String emailContent = templateEngine.process("update-orderStatus", context);

                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());

                helper.setTo(event.getUserEmail());
                helper.setSubject("Cập nhật đơn hàng #" + event.getId());
                helper.setText(emailContent, true); // true: nội dung là HTML
                helper.setFrom(fromEmail);

                mailSender.send(message);
            } catch (MessagingException e) {
                // Log lỗi hoặc xử lý theo yêu cầu nghiệp vụ
                throw new RuntimeException("Lỗi khi gửi email cập nhật trạng thái đơn hàng", e);
            }
        };
    }
}
