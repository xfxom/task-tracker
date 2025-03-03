package com.track.task.controller;

import com.track.task.dto.request.SignIn;
import com.track.task.dto.request.SignUp;
import com.track.task.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "Аутентификация", description = "API для аутентификации пользователей")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Вход в систему", description = "Аутентификация пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешная аутентификация",
                    content = @Content(schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(value = "{\"token\": \"example_token\"}"))),
            @ApiResponse(responseCode = "401", description = "Неверные учетные данные"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public ResponseEntity<?> signin(@RequestBody @Schema(implementation = SignIn.class, example = "{\"username\": \"user\", \"password\": \"password\"}") SignIn signIn) {
        return ResponseEntity.ok(authService.signIn(signIn));
    }

    @PostMapping("/signup")
    @Operation(summary = "Регистрация", description = "Регистрация нового пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешная регистрация",
                    content = @Content(schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(value = "Registration successful"))),
            @ApiResponse(responseCode = "400", description = "Неверный запрос"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public ResponseEntity<?> signup(@RequestBody @Schema(implementation = SignUp.class, example = "{\"username\": \"newuser\", \"password\": \"newpassword\", \"email\": \"newuser@example.com\"}") SignUp signUp) {
        authService.signUp(signUp);
        return ResponseEntity.ok("Registration successful");
    }
}
