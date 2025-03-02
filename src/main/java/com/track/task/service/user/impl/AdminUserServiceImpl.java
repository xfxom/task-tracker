package com.track.task.service.user.impl;

import com.track.task.model.User;
import com.track.task.service.user.AdminUserService;
import com.track.task.service.user.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserService userService;

    @Override
    public Boolean isAdminByEmail(String email) {
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return user.getRoles()
                .stream()
                .anyMatch(role -> role.getName().equals("ADMIN"));
    }
}
