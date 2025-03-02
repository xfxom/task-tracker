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
        log.info("[Task] Creating new task. Initiator: {}", email);

        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> {
                    log.error("[Task] User not found: {}", email);
                    return new UsernameNotFoundException("User not found");
                });

        log.debug("[Task] Fetching priority: {}", taskDto.getPriorityId());
        Priority priority = priorityService.getById(taskDto.getPriorityId())
                .orElseThrow(() -> {
                    log.error("[Task] Priority not found: {}", taskDto.getPriorityId());
                    return new RuntimeException("Priority not found");
                });

        log.debug("[Task] Fetching status: {}", taskDto.getStatusId());
        Status status = statusService.getById(taskDto.getStatusId())
                .orElseThrow(() -> {
                    log.error("[Task] Status not found: {}", taskDto.getStatusId());
                    return new RuntimeException("Status not found");
                });

        Task task = new Task();
        task.setTitle(taskDto.getTitle());
        task.setDescription(taskDto.getDescription());
        task.setUser(user);
        task.setPriority(priority);
        task.setStatus(status);
        task.setVisibility(Boolean.TRUE);

        log.debug("[Task] Processing {} executors", taskDto.getExecutorsIds().size());
        Set<User> executors = taskDto.getExecutorsIds().stream()
                .map(id -> {
                    Optional<User> u = userService.getUserById(id);
                    if (u.isEmpty()) {
                        log.warn("[Task] Executor not found: {}", id);
                    }
                    return u;
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());

        task.setExecutors(new HashSet<>(executors));
        task.setComments(new ArrayList<>());

        Task savedTask = taskRepository.save(task);
        log.info("[Task] Successfully created task ID: {}", savedTask.getId());
        return savedTask;
    }

    private List<Task> getAllVisibleTask(Boolean visible) {
        log.debug("[Task] Fetching all tasks with visibility: {}", visible);
        List<Task> tasks = (visible == null)
                ? taskRepository.findAll()
                : taskRepository.findByVisibility(visible);
        log.debug("[Task] Found {} tasks", tasks.size());
        return tasks;
    }

    private List<Task> getAllVisibleTasksByUserEmail(String email, Boolean visible) {
        log.debug("[Task] Fetching tasks for user: {} visibility: {}", email, visible);
        List<Task> tasks = (visible == null)
                ? taskRepository.findAllByByUserEmail(email)
                : taskRepository.findAllByUserEmailAndVisibility(email, visible);
        log.debug("[Task] Found {} tasks for user {}", tasks.size(), email);
        return tasks;
    }

    public List<Task> getTasks(String email, Boolean visible) {
        log.info("[Task] Getting tasks for: {}", email);
        if (adminUserService.isAdminByEmail(email)) {
            log.debug("[Task] Admin access - fetching all tasks");
            return getAllVisibleTask(visible);
        } else {
            log.debug("[Task] User access - filtering tasks");
            return getAllVisibleTasksByUserEmail(email, visible);
        }
    }

    public Task getTaskById(Long taskId, String userEmail) throws ForbiddenException {
        log.info("[Task] Getting task ID: {}", taskId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    log.error("[Task] Task not found: {}", taskId);
                    return new RuntimeException("Task not found");
                });

        if (adminUserService.isAdminByEmail(userEmail)) {
            log.debug("[Task] Admin access granted");
            return task;
        }

        if (task.getVisibility().equals(Boolean.TRUE)) {
            log.debug("[Task] Visible task access");
            return task;
        }

        if (!task.getUser().getEmail().equals(userEmail)) {
            log.warn("[Task] Unauthorized access attempt by: {}", userEmail);
            throw new ForbiddenException("User is not authorized to view this task");
        }

        log.debug("[Task] Valid owner access");
        return task;
    }

    public void updateTask(Long taskId, TaskDTO taskDto, String userEmail) throws ForbiddenException {
        log.info("[Task] Updating task ID: {}", taskId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    log.error("[Task] Update failed - task not found: {}", taskId);
                    return new RuntimeException("Task not found");
                });

        User user = userService.getUserByEmail(userEmail)
                .orElseThrow(() -> {
                    log.error("[Task] Invalid user: {}", userEmail);
                    return new UsernameNotFoundException("User not found");
                });

        log.debug("[Task] Updating priority: {}", taskDto.getPriorityId());
        Priority priority = priorityService.getById(taskDto.getPriorityId())
                .orElseThrow(() -> {
                    log.error("[Task] Priority not found: {}", taskDto.getPriorityId());
                    return new RuntimeException("Priority not found");
                });

        log.debug("[Task] Updating status: {}", taskDto.getStatusId());
        Status status = statusService.getById(taskDto.getStatusId())
                .orElseThrow(() -> {
                    log.error("[Task] Status not found: {}", taskDto.getStatusId());
                    return new RuntimeException("Status not found");
                });

        log.debug("[Task] Updating title from '{}' to '{}'", task.getTitle(), taskDto.getTitle());
        task.setTitle(taskDto.getTitle());
        task.setDescription(taskDto.getDescription());
        task.setUser(user);
        task.setPriority(priority);
        task.setStatus(status);
        task.setVisibility(taskDto.getVisibility());

        log.debug("[Task] Processing {} comments", taskDto.getCommentsIds().size());
        List<Comment> comments = taskDto.getCommentsIds().stream()
                .map(id -> {
                    Optional<Comment> c = commentService.getById(id);
                    if (c.isEmpty()) {
                        log.warn("[Task] Comment not found: {}", id);
                    }
                    return c;
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        task.getComments().clear();
        task.getComments().addAll(comments);

        log.debug("[Task] Updating {} executors", taskDto.getExecutorsIds().size());
        Set<User> executors = taskDto.getExecutorsIds().stream()
                .map(id -> {
                    Optional<User> u = userService.getUserById(id);
                    if (u.isEmpty()) {
                        log.warn("[Task] Executor not found: {}", id);
                    }
                    return u;
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());

        task.setExecutors(new HashSet<>(executors));

        if (!user.getEmail().equals(userEmail) && !adminUserService.isAdminByEmail(userEmail)) {
            log.error("[Task] Unauthorized update attempt by: {}", userEmail);
            throw new ForbiddenException("Unauthorized");
        }

        taskRepository.save(task);
        log.info("[Task] Successfully updated task ID: {}", taskId);
    }

    public void deleteTask(Long taskId, String userEmail) throws ForbiddenException {
        log.info("[Task] Deleting task ID: {}", taskId);

        if (!taskRepository.existsById(taskId)) {
            log.error("[Task] Delete failed - task not found: {}", taskId);
            throw new NotFoundException("Task not found");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    log.error("[Task] Task inconsistency detected: {}", taskId);
                    return new RuntimeException("Task not found");
                });

        if (!task.getUser().getEmail().equals(userEmail) && !adminUserService.isAdminByEmail(userEmail)) {
            log.error("[Task] Unauthorized delete attempt by: {}", userEmail);
            throw new ForbiddenException("Unauthorized");
        }

        taskRepository.deleteById(taskId);
        log.info("[Task] Successfully deleted task ID: {}", taskId);
    }

    public Task updateTaskStatus(Long taskId, StatusDTO statusDto, String userEmail) throws ForbiddenException {
        log.info("[Task] Updating status for task ID: {}", taskId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    log.error("[Task] Status update failed - task not found: {}", taskId);
                    return new NotFoundException("Task not found");
                });

        if (!isAuthorizedToUpdateTask(task, userEmail)) {
            log.warn("[Task] Unauthorized status update attempt by: {}", userEmail);
            throw new ForbiddenException("User is not authorized to change the status");
        }

        log.debug("[Task] Fetching new status: {}", statusDto.getId());
        Status newStatus = statusService.getById(statusDto.getId())
                .orElseThrow(() -> {
                    log.error("[Task] Status not found: {}", statusDto.getId());
                    return new NotFoundException("Status not found");
                });

        log.debug("[Task] Changing status from {} to {}", task.getStatus().getId(), newStatus.getId());
        task.setStatus(newStatus);

        Task updatedTask = taskRepository.save(task);
        log.info("[Task] Successfully updated status for task ID: {}", taskId);
        return updatedTask;
    }

    private boolean isAuthorizedToUpdateTask(Task task, String userEmail) throws ForbiddenException {
        log.debug("[Task] Checking authorization for: {}", userEmail);
        boolean isAuthorized = isTaskCreator(task.getId(), userEmail) ||
                adminUserService.isAdminByEmail(userEmail) ||
                isExecutorByEmail(task.getId(), userEmail);

        log.debug("[Task] Authorization result: {}", isAuthorized);
        return isAuthorized;
    }

    private boolean isTaskCreator(Long taskId, String email) throws ForbiddenException {
        log.debug("[Task] Checking task creator for: {}", email);
        try {
            Task task = getTaskById(taskId, email);
            User user = userService.getUserByEmail(email)
                    .orElseThrow(() -> {
                        log.error("[Task] User not found: {}", email);
                        return new UsernameNotFoundException("User not found");
                    });
            boolean isCreator = task.getUser().getId().equals(user.getId());
            log.debug("[Task] Creator check result: {}", isCreator);
            return isCreator;
        } catch (ForbiddenException e) {
            log.debug("[Task] Creator check failed");
            return false;
        }
    }

    private Boolean isExecutorByEmail(Long taskId, String email) throws ForbiddenException {
        log.debug("[Task] Checking executor status for: {}", email);
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> {
                    log.error("[Task] User not found: {}", email);
                    return new UsernameNotFoundException("User not found");
                });

        Task task = getTaskById(taskId, email);
        boolean isExecutor = task.getExecutors()
                .stream()
                .anyMatch(executor -> executor.getId().equals(user.getId()));

        log.debug("[Task] Executor check result: {}", isExecutor);
        return isExecutor;
    }
}