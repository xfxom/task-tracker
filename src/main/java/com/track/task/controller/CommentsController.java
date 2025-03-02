package com.track.task.controller;

import com.track.task.dto.CommentDTO;
import com.track.task.model.Comment;
import com.track.task.service.comment.CommentService;
import com.track.task.utils.PaginationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentsController {

    private final CommentService commentService;

    @GetMapping
    public ResponseEntity<?> getAllComments(Principal principal, Pageable pageable) {
        return ResponseEntity.ok().body(
                PaginationUtils.toPage(
                        commentService.getComments(principal.getName()),
                        pageable
                )
        );
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<?> getAllTaskComments(
            @PathVariable
            Long taskId,
            Principal principal,
            Pageable pageable) {
        return ResponseEntity.ok().body(
                PaginationUtils.toPage(
                    commentService.getAllByTaskId(taskId, principal.getName()),
                    pageable
                )
        );
    }

    @PostMapping("/{taskId}")
    public ResponseEntity<?> addComment(
            @PathVariable
            Long taskId,
            @RequestBody CommentDTO commentDto,
            Principal principal) throws Throwable {
        String commenterEmail = principal.getName();
        Comment comment = commentService.addComment(taskId, commentDto, commenterEmail);
        return ResponseEntity.ok(comment);
    }

    @PostMapping("/{taskId}/{parentCommentId}/reply")
    public ResponseEntity<?> replyComment(
            @PathVariable
            Long taskId,
            @PathVariable
            Long parentCommentId,
            @RequestBody CommentDTO commentDto,
            Authentication authentication) {
        String commenterEmail = authentication.getName();
        Comment comment = commentService.replyOnComment(taskId, parentCommentId, commentDto, commenterEmail);
        return ResponseEntity.ok(comment);
    }
}
