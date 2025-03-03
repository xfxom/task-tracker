package com.track.task.user;

import com.track.task.BaseIntegrationTest;
import com.track.task.model.User;
import com.track.task.repository.UserRepository;
import com.track.task.service.user.impl.UserServiceImpl;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
public class UserServiceImplTest extends BaseIntegrationTest {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private UserRepository userRepository;

    private User createUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setUsername("testuser");
        user.setPassword("password");
        return user;
    }

    @Test
    void addUser_ShouldSaveUserWithCorrectData() {
        User newUser = createUser("new@example.com");
        userService.add(newUser);

        Optional<User> savedUser = userRepository.findByEmail("new@example.com");
        assertThat(savedUser)
                .isPresent()
                .hasValueSatisfying(user -> assertThat(user.getUsername()).isEqualTo("testuser"));
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUser() {
        User newUser = createUser("test@example.com");
        userService.add(newUser);
        Long userId = newUser.getId();

        Optional<User> result = userService.getUserById(userId);
        assertThat(result)
                .isPresent()
                .hasValueSatisfying(user -> assertThat(user.getEmail()).isEqualTo("test@example.com"));
    }

    @Test
    void getUserById_WhenUserNotExists_ShouldReturnEmpty() {
        Optional<User> result = userService.getUserById(999L);
        assertThat(result).isEmpty();
    }

    @Test
    void getUserByEmail_WithSpecialCharacters_ShouldWork() {
        String email = "test+filter@example.com";
        User newUser = createUser(email);
        userService.add(newUser);

        Optional<User> result = userService.getUserByEmail(email);
        assertThat(result)
                .isPresent()
                .hasValueSatisfying(user -> assertThat(user.getUsername()).isEqualTo("testuser"));
    }

    @Test
    void existsByEmail_WhenEmailExists_ShouldReturnTrue() {
        String email = "exists@example.com";
        User newUser = createUser(email);
        userService.add(newUser);

        Boolean exists = userService.existsByEmail(email);
        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmail_WhenEmailNotExists_ShouldReturnFalse() {
        Boolean exists = userService.existsByEmail("notexists@example.com");
        assertThat(exists).isFalse();
    }
}
