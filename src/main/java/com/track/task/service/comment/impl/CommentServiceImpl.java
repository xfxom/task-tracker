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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserService userService;
    private final AdminUserService adminUserService;

    @Override
    public void add(Comment comment) {
        commentRepository.save(comment);
    }

    @Override
    public List<Comment> getAll() {
        return commentRepository.findAll();
    }

    @Override
    public Optional<Comment> getById(Long id) {
        return commentRepository.findById(id);
    }

    @Override
    public List<Comment> getAllByEmail(String email) {
        return commentRepository.findAllByUserEmail(email);
    }

    @Override
    public List<Comment> getAllByTaskId(Long taskId, String email) {
        List<Comment> comments = commentRepository.findAllByTaskId(taskId);

        if (adminUserService.isAdminByEmail(email)) return comments;

        return comments.stream()
                .filter(x-> x.getUser().getEmail().equals(email))
                .collect(Collectors.toList());
    }

    @Override
    public List<Comment> getComments(String email) {
        if (adminUserService.isAdminByEmail(email)) {
            return getAll();
        } else {
            return getAllByEmail(email);
        }
    }

    @Override
    public Comment addComment(Long taskId, CommentDTO commentDto, String commenterEmail) throws ForbiddenException {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found"));

        if (task.getVisibility().equals(Boolean.FALSE)) throw new ForbiddenException();

        User commenter = userService.getUserByEmail(commenterEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Comment comment = new Comment();
        comment.setTask(task);
        comment.setUser(commenter);
        comment.setContent(commentDto.getContent());

        if (commentDto.getParentId() != null) {

            Comment parent = getById(commentDto.getParentId())
                    .orElseThrow(() -> new NotFoundException("Comment not found"));
            comment.setParent(parent);
        }

        if (commentDto.getRepliesIds() != null) {
            List<Comment> replies = commentDto.getRepliesIds()
                    .stream()
                    .map(this::getById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList();

            comment.setReplies(replies);
        }

        add(comment);
        return comment;
    }

    @Override
    public Comment replyOnComment(Long taskId, Long parentId, CommentDTO commentDto, String commenterEmail) throws ForbiddenException {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found"));

        User commenter = userService.getUserByEmail(commenterEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Comment parentComment = getById(parentId)
                .orElseThrow(() -> new NotFoundException("Parent comment not found"));

        Comment comment = new Comment();
        comment.setTask(task);
        comment.setUser(commenter);
        comment.setContent(commentDto.getContent());
        comment.setParent(parentComment);

        add(comment);
        return comment;
    }
}
