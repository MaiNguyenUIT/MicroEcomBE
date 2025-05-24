package com.example.comment_service.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReplyCommentResponse {
    private String content;
    private LocalDateTime createdAt = LocalDateTime.now();
    private String userId;
    private String userName;
}
