package com.example.comment_service.event;

import lombok.Data;

@Data
public class ToxicResultEvent {
    private String commentId;
    private boolean isToxic;
}
