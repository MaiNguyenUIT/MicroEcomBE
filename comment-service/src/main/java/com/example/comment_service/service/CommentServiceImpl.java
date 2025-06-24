package com.example.comment_service.service;

import com.example.comment_service.cache.UserClientCacheWrapper;
import com.example.comment_service.dto.CommentDTO;
import com.example.comment_service.dto.CommentResponse;
import com.example.comment_service.dto.UserDTO;
import com.example.comment_service.event.CheckToxicEvent;
import com.example.comment_service.exception.BadRequestException;
import com.example.comment_service.exception.NotFoundException;
import com.example.comment_service.mapper.CommentMapper;
import com.example.comment_service.mapper.CommentResponseMapper;
import com.example.comment_service.model.Comment;
import com.example.comment_service.model.ReplyComment;
import com.example.comment_service.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService{
    @Autowired
    private CommentRepository commentRepository;

    private final StreamBridge streamBridge;
    public CommentServiceImpl(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    @Autowired
    private UserClientCacheWrapper userClientCacheWrapper;

    @Override
    public Comment createComment(String productId, CommentDTO commentDTO) {
        Comment comment = new Comment();
        comment.setDescription(commentDTO.getDescription());
        comment.setUserId(SecurityContextHolder.getContext().getAuthentication().getName());
        comment.setProductId(productId);
        Comment createdComment = commentRepository.save(comment);

        CheckToxicEvent checkToxicEvent = new CheckToxicEvent();
        checkToxicEvent.setCommentDescription(createdComment.getDescription());
        checkToxicEvent.setCommentId(createdComment.getId());

        streamBridge.send("sendCheckToxic-out-0", checkToxicEvent);

        return createdComment;
    }

    @Override
    public Comment updateComment(String commentId, CommentDTO commentDTO) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment is not exist with id: " + commentId));

        comment.setDescription(commentDTO.getDescription());
        comment.setUpdatedAt(LocalDateTime.now());
        commentRepository.save(comment);

        CheckToxicEvent checkToxicEvent = new CheckToxicEvent();
        checkToxicEvent.setCommentDescription(comment.getDescription());
        checkToxicEvent.setCommentId(comment.getId());

        streamBridge.send("sendCheckToxic-out-0", checkToxicEvent);
        return comment;
    }

    @Override
    public void deleteComment(String id) {
        Comment comment = commentRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Comment is not found with id: " + id)
        );
        if (!comment.getUserId().equals(SecurityContextHolder.getContext().getAuthentication().getName())){
            throw new BadRequestException("You cannot delete other people's comments.");
        } else {
            commentRepository.deleteById(id);
        }
    }

    @Override
    public Page<CommentResponse> getAllCommentByProductId(String productId, int page, int size, String sortField, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("desc") ? Sort.by(sortField).descending() : Sort.by(sortField).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Comment> comments = commentRepository.findByproductId(productId, pageable);

        List<String> userIds = comments.getContent().stream()
                .map(Comment::getUserId)
                .toList();

        List<UserDTO> userDTOS = userClientCacheWrapper.getUserByIds(userIds);

        Map<String, String> userIdToDisplayName = userDTOS.stream()
                .collect(Collectors.toMap(UserDTO::getId, UserDTO::getDisplayName));

        List<CommentResponse> commentResponses = comments.stream()
                .map(comment -> {
                    CommentResponse commentResponse = new CommentResponse();

                    commentResponse.setId(comment.getId());
                    commentResponse.setProductId(comment.getProductId());
                    commentResponse.setReplyComment(comment.getReplyComment());
                    commentResponse.setDescription(comment.getDescription());
                    commentResponse.setUserId(comment.getUserId());
                    commentResponse.setUpdatedAt(comment.getUpdatedAt());
                    commentResponse.setToxic(comment.isToxic());
                    commentResponse.setCreatedAt(comment.getCreatedAt());
                    commentResponse.setDisplayName(userIdToDisplayName.get(comment.getUserId()));

                    return commentResponse;
                })
                .toList();

        return new PageImpl<>(commentResponses, pageable, comments.getTotalElements());
    }

    @Override
    public Comment replyComment(String id, ReplyComment replyComment) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Comment is not exist with id: " + id));
        comment.getReplyComment().add(replyComment);
        return commentRepository.save(comment);
    }
}
