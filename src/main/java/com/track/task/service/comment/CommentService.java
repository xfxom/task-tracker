package com.track.task.service.comment;

import com.track.task.dto.CommentDTO;
import com.track.task.exception.ForbiddenException;
import com.track.task.model.Comment;

import java.util.List;
import java.util.Optional;


public interface CommentService {
    void add(Comment comment);
    List<Comment> getAll();
    Optional<Comment> getById(Long id);
    List<Comment> getAllByEmail(String email);
    List<Comment> getAllByTaskId(Long taskId, String email);
    List<Comment> getComments(String email);
    Comment addComment(Long taskId, CommentDTO commentDto, String commenterEmail) throws ForbiddenException;
    Comment replyOnComment(Long taskId, Long parentId, CommentDTO commentDto, String commenterEmail) throws ForbiddenException;
}
