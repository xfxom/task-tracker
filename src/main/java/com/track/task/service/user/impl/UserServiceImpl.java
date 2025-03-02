package com.track.task.service.impl.user;

import com.track.task.model.User;
import com.track.task.repository.UserRepository;
import com.track.task.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

@Slf4j
@AllArgsConstructor
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

    public Boolean isAdminByEmail(String email) {
        User user = getUserByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return user.getRoles()
                .stream()
                .anyMatch(role -> role.getName().equals("ADMIN"));
    }
}
