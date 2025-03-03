package com.track.task.controller;

import com.track.task.dto.StatusDTO;
import com.track.task.exception.ForbiddenException;
import com.track.task.service.status.StatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/status")
@RequiredArgsConstructor
@Tag(name = "Статус", description = "API для управления статусами")
public class StatusController {

    private final StatusService statusService;

    @GetMapping
    @Operation(
            summary = "Получить все статусы",
            description = "Получение списка всех статусов"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешная операция",
                    content = @Content(
                            schema = @Schema(implementation = StatusDTO.class),
                            examples = @ExampleObject(
                                    value = "[{\"id\": 1, \"title\": \"В процессе\", \"isGlobal\": true, \"userId\": 101}, " +
                                            "{\"id\": 2, \"title\": \"Завершено\", \"isGlobal\": false, \"userId\": 102}]"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Статусы не найдены",
                    content = @Content
            )
    })
    public ResponseEntity<?> getAllStatus() {
        return ResponseEntity.ok().body(statusService.getAll());
    }

    @GetMapping("/{statusId}")
    @Operation(
            summary = "Получить статус по ID",
            description = "Получение статуса по его ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешная операция",
                    content = @Content(
                            schema = @Schema(implementation = StatusDTO.class),
                            examples = @ExampleObject(
                                    value = "{\"id\": 1, \"title\": \"В процессе\", \"isGlobal\": true, \"userId\": 101}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Статус не найден",
                    content = @Content
            )
    })
    public ResponseEntity<?> getById(
            @Parameter(description = "ID статуса для получения", example = "1")
            @PathVariable Long statusId
    ) {
        return ResponseEntity.ok().body(statusService.getById(statusId));
    }

    @PostMapping
    @Operation(
            summary = "Добавить новый статус",
            description = "Создание нового статуса. isGlobal - определяет, статус является локальным для данного пользователя " +
                    "или его могут использовать все пользователи. Глобальные статусы может добавлять только админ"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешная операция",
                    content = @Content(
                            schema = @Schema(implementation = StatusDTO.class),
                            examples = @ExampleObject(
                                    value = "{\"id\": 3, \"title\": \"Новый статус\", \"isGlobal\": false, \"userId\": 103}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Неверный ввод",
                    content = @Content
            )
    })
    public ResponseEntity<?> addStatus(
            @RequestBody
            @Schema(
                    implementation = StatusDTO.class,
                    example = "{\"title\": \"Новый статус\", \"isGlobal\": false}"
            ) StatusDTO statusDTO,
            Principal principal
    ) {
        return ResponseEntity.ok().body(statusService.add(statusDTO, principal.getName()));
    }

    @PutMapping("/{statusId}")
    @Operation(
            summary = "Обновить существующий статус",
            description = "Обновление статуса по его ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешная операция",
                    content = @Content(
                            schema = @Schema(implementation = StatusDTO.class),
                            examples = @ExampleObject(
                                    value = "{\"id\": 1, \"title\": \"Обновленный статус\", \"isGlobal\": true, \"userId\": 101}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Неверный ввод",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Статус не найден",
                    content = @Content
            )
    })
    public ResponseEntity<?> updateStatus(
            @Parameter(description = "ID статуса для обновления", example = "1")
            @PathVariable Long statusId,
            @RequestBody
            @Schema(
                    implementation = StatusDTO.class,
                    example = "{\"title\": \"Обновленный статус\", \"isGlobal\": true}"
            ) StatusDTO statusDTO
    ) {
        return ResponseEntity.ok().body(statusService.update(statusId, statusDTO));
    }

    @DeleteMapping("/{statusId}")
    @Operation(
            summary = "Удалить статус",
            description = "Удаление статуса по его ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Успешная операция"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Статус не найден",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Доступ запрещен",
                    content = @Content
            )
    })
    public ResponseEntity<?> deleteStatus(
            @Parameter(description = "ID статуса для удаления", example = "1")
            @PathVariable Long statusId,
            Principal principal
    ) {
        try {
            statusService.delete(statusId, principal.getName());
            return ResponseEntity.noContent().build();
        } catch (ForbiddenException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }
}