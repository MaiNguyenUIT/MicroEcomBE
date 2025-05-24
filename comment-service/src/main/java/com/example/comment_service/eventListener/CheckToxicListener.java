package com.example.comment_service.eventListener;

import com.example.comment_service.event.ToxicResultEvent;
import com.example.comment_service.exception.NotFoundException;
import com.example.comment_service.model.Comment;
import com.example.comment_service.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
public class CheckToxicListener {
    @Autowired
    private CommentRepository commentRepository;
    @Bean
    public Consumer<ToxicResultEvent> sendCheckToxicTrue (){
        return toxicResultEvent -> {
            System.out.println("Check toxic true");
            Comment comment = commentRepository.findById(toxicResultEvent.getCommentId()).orElseThrow(
                    () -> new NotFoundException("Toxic is not found with id: " + toxicResultEvent.getCommentId())
            );

            comment.setToxic(true);
            commentRepository.save(comment);
        };
    }

    @Bean
    public Consumer<ToxicResultEvent> sendCheckToxicFalse (){
        return toxicResultEvent -> {
            System.out.println("Check toxic false");
            Comment comment = commentRepository.findById(toxicResultEvent.getCommentId()).orElseThrow(
                    () -> new NotFoundException("Toxic is not found with id: " + toxicResultEvent.getCommentId())
            );

            comment.setToxic(false);
            commentRepository.save(comment);
        };
    }
}
