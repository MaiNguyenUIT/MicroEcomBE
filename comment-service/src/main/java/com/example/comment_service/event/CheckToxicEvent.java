package com.example.comment_service.event;

import lombok.Data;

@Data
public class CheckToxicEvent {
    private String commentId;
    private String commentDescription;
}
