package com.track.task.service.user;


import com.track.task.model.User;

import java.util.Optional;

public interface UserService {
    void add(User user);
    Optional<User> getUserById(Long id);
    Optional<User> getUserByEmail(String email);
    Boolean existsByEmail(String email);
}
