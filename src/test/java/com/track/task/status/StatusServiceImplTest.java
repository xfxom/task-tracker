package com.track.task.status;

import com.track.task.exception.ForbiddenException;
import com.track.task.model.User;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import com.track.task.model.Status;
import com.track.task.dto.StatusDTO;
import com.track.task.repository.StatusRepository;
import com.track.task.service.status.impl.StatusServiceImpl;
import com.track.task.service.user.UserService;
import com.track.task.service.user.AdminUserService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@Transactional
@ExtendWith(MockitoExtension.class)
public class StatusServiceImplTest {

    @InjectMocks
    private StatusServiceImpl statusService;

    @Mock
    private StatusRepository statusRepository;

    @Mock
    private UserService userService;

    @Mock
    private AdminUserService adminUserService;

    @Test
    void testAddStatus() {

        StatusDTO statusDTO = new StatusDTO();
        statusDTO.setTitle("New Status");
        statusDTO.setUserId(1L);
        statusDTO.setIsGlobal(false);

        Status status = new Status();
        status.setId(1L);
        status.setTitle("New Status");

        when(adminUserService.isAdminByEmail("admin@example.com")).thenReturn(true);
        when(userService.getUserById(1L)).thenReturn(Optional.of(new User())); // Assuming User class exists
        when(statusRepository.save(any(Status.class))).thenReturn(status);

        Status createdStatus = statusService.add(statusDTO, "admin@example.com");

        assertThat(createdStatus.getTitle()).isEqualTo("New Status");
        verify(statusRepository).save(any(Status.class));
    }

    @Test
    void testUpdateStatus() {

        StatusDTO statusDTO = new StatusDTO();
        statusDTO.setTitle("Updated Status");

        Status status = new Status();
        status.setId(1L);
        status.setTitle("Old Status");

        when(statusRepository.findById(1L)).thenReturn(Optional.of(status));
        when(statusRepository.save(any(Status.class))).thenReturn(status);

        Status updatedStatus = statusService.update(1L, statusDTO);

        assertThat(updatedStatus.getTitle()).isEqualTo("Updated Status");
        verify(statusRepository).save(any(Status.class));
    }

    @Test
    void testDeleteForbiddenStatusByNonAdmin() {

        Status status = new Status();
        status.setId(1L);
        status.setTitle("Non-Global Status");
        status.setIsGlobal(false);
        User author = new User();
        author.setEmail("author@example.com");
        status.setUser(author);

        when(statusRepository.findById(1L)).thenReturn(Optional.of(status));
        when(adminUserService.isAdminByEmail("nonadmin@example.com")).thenReturn(false);

        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> statusService.delete(1L, "nonadmin@example.com"));

        assertThat(exception.getMessage()).isEqualTo("Forbidden exception: User is not authorized to delete status");
    }

    @Test
    void testDeleteStatusByAdmin() {

        Status status = new Status();
        status.setId(1L);
        status.setTitle("Non-Global Status");
        status.setIsGlobal(false);
        User author = new User();
        author.setEmail("author@example.com");
        status.setUser(author);

        when(statusRepository.findById(1L)).thenReturn(Optional.of(status));
        when(adminUserService.isAdminByEmail("admin@example.com")).thenReturn(true);

        // When
        statusService.delete(1L, "admin@example.com");

        verify(statusRepository).deleteById(1L);
    }



}
