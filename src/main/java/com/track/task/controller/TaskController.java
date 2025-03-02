package com.track.task.controller;

import com.track.task.dto.StatusDTO;
import com.track.task.dto.TaskDTO;
import com.track.task.model.Task;
import com.track.task.service.task.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    public ResponseEntity<?> getTasks(
            Principal principal,
            @RequestParam(required = false) Boolean visible) {
        return ResponseEntity.ok().body(taskService.getTasks(principal.getName(), visible));
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<?> getTaskById(
            @PathVariable Long taskId,
            Authentication authentication) {
        String userEmail = authentication.getName();
        return ResponseEntity.ok(taskService.getTaskById(taskId, userEmail));
    }

    @PostMapping
    public ResponseEntity<Task> createTask(
            @RequestBody
            TaskDTO taskDto,
            Principal principal) {
        Task createdTask = taskService.createTask(taskDto, principal.getName());
        return ResponseEntity.ok(createdTask);
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<?> updateTask(
            @PathVariable Long taskId,
            @RequestBody TaskDTO taskDto,
            Principal principal) {
        taskService.updateTask(taskId, taskDto, principal.getName());
        return ResponseEntity.ok("Success update");
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<?> deleteTask(
            @PathVariable
            Long taskId,
            Principal principal) {

        taskService.deleteTask(taskId, principal.getName());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{taskId}/status")
    public ResponseEntity<Task> updateStatus(
            @PathVariable Long taskId,
            @RequestBody StatusDTO statusDto,
            Authentication authentication) {
        String executorEmail = authentication.getName();
        Task updatedTask = taskService.updateTaskStatus(taskId, statusDto, executorEmail);
        return ResponseEntity.ok(updatedTask);
    }
}
