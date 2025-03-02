package com.track.task.controller;


import com.track.task.dto.request.SignIn;
import com.track.task.dto.request.SignUp;
import com.track.task.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> signin(@RequestBody SignIn signIn) {
        try {
            return ResponseEntity.ok(authService.signIn(signIn));
        } catch (BadCredentialsException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignUp signUp) {
        authService.signUp(signUp);
        return ResponseEntity.ok("Registration successful");
    }
}
