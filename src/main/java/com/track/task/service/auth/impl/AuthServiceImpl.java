package com.track.task.service.auth.impl;

import com.track.task.dto.request.SignIn;
import com.track.task.dto.request.SignUp;
import com.track.task.dto.response.AuthenticationResponse;
import com.track.task.exception.EmptyException;
import com.track.task.exception.ExistsException;
import com.track.task.model.User;
import com.track.task.security.JwtUtil;
import com.track.task.service.auth.AuthService;
import com.track.task.service.user.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;

    @Lazy
    private final UserService userService;

    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AuthenticationResponse signIn(SignIn signIn) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(signIn.getEmail(), signIn.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwtToken = jwtUtil.generateToken(authentication);
        log.info("User: " + signIn.getEmail() + " is login");
        return new AuthenticationResponse(jwtToken);
    }

    @Override
    public void signUp(SignUp signUp) {

        if (signUp.getEmail().isEmpty() || signUp.getPassword().isEmpty())
            throw new EmptyException("user");

        if (userService.existsByEmail(signUp.getEmail()))
            throw new ExistsException("email");

        User user = new User();
        user.setUsername(signUp.getUsername());
        user.setEmail(signUp.getEmail());
        user.setPassword(passwordEncoder.encode(signUp.getPassword()));

        userService.add(user);
    }
}
