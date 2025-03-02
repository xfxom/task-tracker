package com.track.task.service.task.impl;

import com.track.task.dto.StatusDTO;
import com.track.task.dto.TaskDTO;
import com.track.task.exception.ForbiddenException;
import com.track.task.model.Priority;
import com.track.task.model.Status;
import com.track.task.model.Task;
import com.track.task.model.User;
import com.track.task.repository.TaskRepository;
import com.track.task.service.comment.CommentService;
import com.track.task.service.priority.PriorityService;
import com.track.task.service.status.StatusService;
import com.track.task.service.task.TaskService;
import com.track.task.service.user.AdminUserService;
import com.track.task.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.track.task.model.Comment;
import com.track.task.exception.NotFoundException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final UserService userService;
    private final PriorityService priorityService;
    private final StatusService statusService;
    private final CommentService commentService;
    private final AdminUserService adminUserService;

    @Override
    public Task createTask(TaskDTO taskDto, String email) {

        log.info("Create task");

        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Priority priority = priorityService.getById(taskDto.getPriorityId())
                .orElseThrow(() -> new RuntimeException("Priority not found"));

        Status status = statusService.getById(taskDto.getStatusId())
                .orElseThrow(() -> new RuntimeException("Status not found"));

        Task task = new Task();
        task.setTitle(taskDto.getTitle());
        task.setDescription(taskDto.getDescription());
        task.setUser(user);
        task.setPriority(priority);
        task.setStatus(status);
        task.setVisibility(Boolean.TRUE);

        Set<User> executors = taskDto.getExecutorsIds().stream()
                .map(userService::getUserById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());

        task.setExecutors(new HashSet<>(executors));
        task.setComments(new ArrayList<>());

        return taskRepository.save(task);
    }

    private List<Task> getAllVisibleTask(Boolean visible) {
        if (visible == null)
            return taskRepository.findAll();

        return taskRepository.findByVisibility(visible);
    }

    private List<Task> getAllVisibleTasksByUserEmail(String email, Boolean visible) {
        if (visible == null)
            return taskRepository.findAllByByUserEmail(email);

        return taskRepository.findAllByUserEmailAndVisibility(email, visible);
    }

    public List<Task> getTasks(String email, Boolean visible) {
        if (adminUserService.isAdminByEmail(email)) {
            return getAllVisibleTask(visible);
        } else {
            return getAllVisibleTasksByUserEmail(email, visible);
        }
    }

    public Task getTaskById(Long taskId, String userEmail) throws ForbiddenException {

        log.info("Get task by id: " + taskId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (adminUserService.isAdminByEmail(userEmail) || task.getVisibility().equals(Boolean.TRUE)) {
            return task;
        }

        if (!task.getUser().getEmail().equals(userEmail)) {
            throw new ForbiddenException("User is not authorized to view this task");
        }

        return task;
    }

    public void updateTask(Long taskId, TaskDTO taskDto, String userEmail) throws ForbiddenException {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        User user = userService.getUserByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Priority priority = priorityService.getById(taskDto.getPriorityId())
                .orElseThrow(() -> new RuntimeException("Priority not found"));

        Status status = statusService.getById(taskDto.getStatusId())
                .orElseThrow(() -> new RuntimeException("Status not found"));

        task.setTitle(taskDto.getTitle());
        task.setDescription(taskDto.getDescription());
        task.setUser(user);
        task.setPriority(priority);
        task.setStatus(status);
        task.setVisibility(taskDto.getVisibility());

        List<Comment> comments = taskDto.getCommentsIds().stream()
                .map(commentService::getById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        task.getComments().clear();
        task.getComments().addAll(comments);

        Set<User> executors = taskDto.getExecutorsIds().stream()
                .map(userService::getUserById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());

        task.setExecutors(new HashSet<>(executors));

        if (!user.getEmail().equals(userEmail) && !adminUserService.isAdminByEmail(userEmail)) {
            log.error("Unauthorized");
            throw new ForbiddenException("Unauthorized");
        }

        log.info("Task success updated");
        taskRepository.save(task);
    }

    public void deleteTask(Long taskId, String userEmail) throws ForbiddenException {
        if (!taskRepository.existsById(taskId)) {
            throw new NotFoundException("Task not found");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!task.getUser().getEmail().equals(userEmail) && !adminUserService.isAdminByEmail(userEmail)) {
            log.error("Delete: Unauthorized");
            throw new ForbiddenException("Unauthorized");
        }

        log.info("Success delete task");
        taskRepository.deleteById(taskId);
    }

    public Task updateTaskStatus(Long taskId, StatusDTO statusDto, String userEmail) throws ForbiddenException {

        log.info("Update task status");

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found"));

        if (!isAuthorizedToUpdateTask(task, userEmail)) {
            throw new ForbiddenException("User is not authorized to change the status");
        }

        Status newStatus = statusService.getById(statusDto.getId())
                .orElseThrow(() -> new NotFoundException("Status not found"));

        task.setStatus(newStatus);

        return taskRepository.save(task);
    }

    private boolean isAuthorizedToUpdateTask(Task task, String userEmail) throws ForbiddenException {
        return isTaskCreator(task.getId(), userEmail) ||
                adminUserService.isAdminByEmail(userEmail) ||
                isExecutorByEmail(task.getId(), userEmail);
    }


    private boolean isTaskCreator(Long taskId, String email) throws ForbiddenException {

        Task task = getTaskById(taskId, email);

        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return task.getUser().getId().equals(user.getId());
    }


    private Boolean isExecutorByEmail(Long taskId, String email) throws ForbiddenException {

        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Task task = getTaskById(taskId, email);

        return task.getExecutors()
                .stream()
                .anyMatch(executors -> executors.getId().equals(user.getId()));
    }
}
