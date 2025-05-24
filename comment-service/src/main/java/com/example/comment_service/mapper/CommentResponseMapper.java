package com.example.comment_service.mapper;

import com.example.comment_service.dto.CommentResponse;
import com.example.comment_service.model.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CommentResponseMapper {
    CommentResponseMapper INSTANCE = Mappers.getMapper(CommentResponseMapper.class);
    @Mapping(target = "displayName", ignore = true)
    CommentResponse toResponse(Comment comment);
}
