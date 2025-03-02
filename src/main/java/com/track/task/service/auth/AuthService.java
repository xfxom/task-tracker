package com.track.task.service.auth;


import com.track.task.dto.request.SignIn;
import com.track.task.dto.request.SignUp;
import com.track.task.dto.response.AuthenticationResponse;

public interface AuthService {
    AuthenticationResponse signIn(SignIn signIn);
    void signUp(SignUp signUp);
}