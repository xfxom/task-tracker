package com.track.task.controller;

import com.track.task.dto.StatusDTO;
import com.track.task.dto.TaskDTO;
import com.track.task.model.Task;
import com.track.task.service.task.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Задачи", description = "API для управления задачами")
public class TaskController {

    private final TaskService taskService;

    @Operation(
            summary = "Получить список задач",
            description = "Возвращает список задач с возможностью фильтрации по видимости",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Список задач успешно получен",
                            content = @Content(schema = @Schema(implementation = Task.class))

                    )
            }
    )
    @GetMapping
    public ResponseEntity<?> getTasks(
            Principal principal,
            @Parameter(description = "Флаг видимости задач", example = "true")
            @RequestParam(required = false) Boolean visible) {
        return ResponseEntity.ok().body(taskService.getTasks(principal.getName(), visible));
    }

    @Operation(
            summary = "Получить задачу по ID",
            description = "Возвращает задачу по указанному идентификатору",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Задача успешно найдена",
                            content = @Content(schema = @Schema(implementation = Task.class))),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Задача не найдена",
                            content = @Content)
            }
    )
    @GetMapping("/{taskId}")
    public ResponseEntity<?> getTaskById(
            @Parameter(description = "ID задачи", example = "1")
            @PathVariable Long taskId,
            Authentication authentication) {
        String userEmail = authentication.getName();
        return ResponseEntity.ok(taskService.getTaskById(taskId, userEmail));
    }

    @Operation(
            summary = "Создать новую задачу",
            description = "Создает новую задачу с указанными параметрами",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TaskDTO.class),
                            examples = @ExampleObject(
                                    name = "Пример создания задачи",
                                    value = "{\"title\": \"Новая задача\", \"description\": \"Описание задачи\", \"statusId\": 1}"))
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Задача успешно создана",
                            content = @Content(schema = @Schema(implementation = Task.class))),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Неверные параметры запроса",
                            content = @Content)
            }
    )
    @PostMapping
    public ResponseEntity<Task> createTask(
            @RequestBody TaskDTO taskDto,
            Principal principal) {
        Task createdTask = taskService.createTask(taskDto, principal.getName());
        return ResponseEntity.ok(createdTask);
    }

    @Operation(
            summary = "Обновить задачу",
            description = "Обновляет существующую задачу",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Задача успешно обновлена",
                            content = @Content(schema = @Schema(example = "Success update"))),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Задача не найдена",
                            content = @Content),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Доступ запрещен",
                            content = @Content)
            }
    )
    @PutMapping("/{taskId}")
    public ResponseEntity<?> updateTask(
            @Parameter(description = "ID задачи", example = "1")
            @PathVariable Long taskId,
            @RequestBody TaskDTO taskDto,
            Principal principal) {
        taskService.updateTask(taskId, taskDto, principal.getName());
        return ResponseEntity.ok("Success update");
    }

    @Operation(
            summary = "Удалить задачу",
            description = "Удаляет задачу по указанному идентификатору",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Задача успешно удалена"),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Задача не найдена"),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Доступ запрещен")
            }
    )
    @DeleteMapping("/{taskId}")
    public ResponseEntity<?> deleteTask(
            @Parameter(description = "ID задачи", example = "1")
            @PathVariable Long taskId,
            Principal principal) {
        taskService.deleteTask(taskId, principal.getName());
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Обновить статус задачи",
            description = "Изменяет статус указанной задачи",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StatusDTO.class),
                            examples = @ExampleObject(
                                    name = "Пример обновления статуса",
                                    value = "{\"id\": 2}"))
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Статус задачи успешно обновлен",
                            content = @Content(schema = @Schema(implementation = Task.class))),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Задача или статус не найдены",
                            content = @Content),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Доступ запрещен",
                            content = @Content)
            }
    )
    @PatchMapping("/{taskId}/status")
    public ResponseEntity<Task> updateStatus(
            @Parameter(description = "ID задачи", example = "1")
            @PathVariable Long taskId,
            @RequestBody StatusDTO statusDto,
            Authentication authentication) {
        String executorEmail = authentication.getName();
        Task updatedTask = taskService.updateTaskStatus(taskId, statusDto, executorEmail);
        return ResponseEntity.ok(updatedTask);
    }
}