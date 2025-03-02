package com.track.task.service.comment.impl;

import com.track.task.dto.CommentDTO;
import com.track.task.exception.ForbiddenException;
import com.track.task.exception.NotFoundException;
import com.track.task.model.Comment;
import com.track.task.model.Task;
import com.track.task.model.User;
import com.track.task.repository.CommentRepository;
import com.track.task.repository.TaskRepository;
import com.track.task.service.comment.CommentService;
import com.track.task.service.user.AdminUserService;
import com.track.task.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserService userService;
    private final AdminUserService adminUserService;

    @Override
    public void add(Comment comment) {
        log.info("Adding comment with id: {}", comment.getId());
        commentRepository.save(comment);
        log.debug("Comment with id: {} successfully saved", comment.getId());
    }

    @Override
    public List<Comment> getAll() {
        log.info("Fetching all comments");
        List<Comment> comments = commentRepository.findAll();
        log.debug("Found {} comments", comments.size());
        return comments;
    }

    @Override
    public Optional<Comment> getById(Long id) {
        log.info("Fetching comment by id: {}", id);
        Optional<Comment> comment = commentRepository.findById(id);
        if (comment.isPresent()) {
            log.debug("Comment with id: {} found", id);
        } else {
            log.warn("Comment with id: {} not found", id);
        }
        return comment;
    }

    @Override
    public List<Comment> getAllByEmail(String email) {
        log.info("Fetching comments for email: {}", email);
        List<Comment> comments = commentRepository.findAllByUserEmail(email);
        log.debug("Found {} comments for email: {}", comments.size(), email);
        return comments;
    }

    @Override
    public List<Comment> getAllByTaskId(Long taskId, String email) {
        log.info("Fetching comments for taskId: {} and email: {}", taskId, email);
        List<Comment> comments = commentRepository.findAllByTaskId(taskId);

        if (adminUserService.isAdminByEmail(email)) {
            log.debug("User {} is admin, returning all {} comments", email, comments.size());
            return comments;
        }

        List<Comment> filtered = comments.stream()
                .filter(x -> x.getUser().getEmail().equals(email))
                .collect(Collectors.toList());
        log.debug("Filtered {} comments for non-admin user {}", filtered.size(), email);
        return filtered;
    }

    @Override
    public List<Comment> getComments(String email) {
        log.info("Fetching comments for user: {}", email);
        if (adminUserService.isAdminByEmail(email)) {
            log.debug("Admin access granted for {}", email);
            return getAll();
        } else {
            log.debug("Regular user access for {}", email);
            return getAllByEmail(email);
        }
    }

    @Override
    public Comment addComment(Long taskId, CommentDTO commentDto, String commenterEmail) throws ForbiddenException {
        log.info("Adding new comment to task: {} by user: {}", taskId, commenterEmail);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    log.error("Task not found with id: {}", taskId);
                    return new NotFoundException("Task not found");
                });

        if (!task.getVisibility()) {
            log.error("Attempt to comment on hidden task: {}", taskId);
            throw new ForbiddenException();
        }

        User commenter = userService.getUserByEmail(commenterEmail)
                .orElseThrow(() -> {
                    log.error("User not found: {}", commenterEmail);
                    return new UsernameNotFoundException("User not found");
                });

        Comment comment = new Comment();
        comment.setTask(task);
        comment.setUser(commenter);
        comment.setContent(commentDto.getContent());
        log.debug("Created comment base: {}", comment);

        if (commentDto.getParentId() != null) {
            Comment parent = getById(commentDto.getParentId())
                    .orElseThrow(() -> {
                        log.error("Parent comment not found: {}", commentDto.getParentId());
                        return new NotFoundException("Comment not found");
                    });
            comment.setParent(parent);
            log.debug("Set parent comment: {}", parent.getId());
        }

        if (commentDto.getRepliesIds() != null) {
            List<Comment> replies = commentDto.getRepliesIds()
                    .stream()
                    .map(this::getById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList();

            comment.setReplies(replies);
            log.debug("Added {} replies to comment", replies.size());
        }

        add(comment);
        log.info("Successfully added comment with id: {}", comment.getId());
        return comment;
    }

    @Override
    public Comment replyOnComment(Long taskId, Long parentId, CommentDTO commentDto, String commenterEmail) {
        log.info("Replying to comment: {} on task: {} by user: {}", parentId, taskId, commenterEmail);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    log.error("Task not found: {}", taskId);
                    return new NotFoundException("Task not found");
                });

        User commenter = userService.getUserByEmail(commenterEmail)
                .orElseThrow(() -> {
                    log.error("User not found: {}", commenterEmail);
                    return new UsernameNotFoundException("User not found");
                });

        Comment parentComment = getById(parentId)
                .orElseThrow(() -> {
                    log.error("Parent comment not found: {}", parentId);
                    return new NotFoundException("Parent comment not found");
                });

        Comment comment = new Comment();
        comment.setTask(task);
        comment.setUser(commenter);
        comment.setContent(commentDto.getContent());
        comment.setParent(parentComment);
        log.debug("Created reply comment: {}", comment);

        add(comment);
        log.info("Successfully added reply comment with id: {}", comment.getId());
        return comment;
    }
}