package com.example.comment_service.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReplyComment {
    private String content;
    private LocalDateTime createdAt = LocalDateTime.now();
    private String userId;
}
