package com.track.task.service.user.impl;

import com.track.task.model.User;
import com.track.task.repository.UserRepository;
import com.track.task.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public void add(User user) {
        log.info("Add user");
        userRepository.save(user);
    }

    @Override
    public Optional<User> getUserById(Long id) {
        log.info("Get user by id");
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        log.info("Get user by email");
        return userRepository.findByEmail(email);
    }

    @Override
    public Boolean existsByEmail(String email) {
        log.info("Exists user by email");
        return userRepository.existsByEmail(email);
    }
}
