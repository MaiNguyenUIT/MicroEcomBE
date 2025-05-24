package com.example.comment_service.controller;

import com.example.comment_service.dto.CommentDTO;
import com.example.comment_service.dto.CommentResponse;
import com.example.comment_service.model.Comment;
import com.example.comment_service.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/comment")
public class CommentController {
    @Autowired
    private CommentService commentService;
    @PostMapping()
    public ResponseEntity<Comment> createComment(@RequestBody CommentDTO commentDTO, @RequestParam String productId){
        return ResponseEntity.ok(commentService.createComment(productId, commentDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Comment> updateComment(@RequestBody CommentDTO commentDTO, @PathVariable String id){
        return ResponseEntity.ok(commentService.createComment(id, commentDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteComment(@PathVariable String id){
        commentService.deleteComment(id);
        return ResponseEntity.ok("Delete comment successfully");
    }

    @GetMapping("")
    public ResponseEntity<Page<CommentResponse>> getCommentByProductId(@RequestParam String productId,
                                                                       @RequestParam(defaultValue = "0") int page,
                                                                       @RequestParam(defaultValue = "10") int size,
                                                                       @RequestParam(defaultValue = "createdAt") String sortField,
                                                                       @RequestParam(defaultValue = "desc") String sortDirection){
        return ResponseEntity.ok(commentService.getAllCommentByProductId(productId, page, size, sortField, sortDirection));
    }
}
