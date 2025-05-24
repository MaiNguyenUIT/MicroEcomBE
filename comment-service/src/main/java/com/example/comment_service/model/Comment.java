package com.example.comment_service.model;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class Comment {
    @Id
    private String id;
    private String description;
    private LocalDateTime createdAt = LocalDateTime.now();
    private String userId;
    private String productId;
    private List<ReplyComment> replyComment = new ArrayList<>();
    private LocalDateTime updatedAt;
    private boolean isToxic = false;
}
