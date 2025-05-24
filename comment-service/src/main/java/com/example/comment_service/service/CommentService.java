package com.example.comment_service.service;

import com.example.comment_service.dto.CommentDTO;
import com.example.comment_service.dto.CommentResponse;
import com.example.comment_service.model.Comment;
import com.example.comment_service.model.ReplyComment;
import org.springframework.data.domain.Page;

public interface CommentService {
    Comment createComment(String productId, CommentDTO commentDTO);
    Comment updateComment(String commentId, CommentDTO commentDTO);
    void deleteComment(String id);
    Page<CommentResponse> getAllCommentByProductId(String productId, int page, int size, String sortField, String sortDirection);
    Comment replyComment(String id, ReplyComment replyComment);
}
