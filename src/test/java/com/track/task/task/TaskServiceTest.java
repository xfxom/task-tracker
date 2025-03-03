package com.track.task.task;

import com.track.task.BaseIntegrationTest;
import com.track.task.dto.StatusDTO;
import com.track.task.dto.TaskDTO;
import com.track.task.exception.ForbiddenException;
import com.track.task.model.Priority;
import com.track.task.model.Status;
import com.track.task.model.Task;
import com.track.task.model.User;
import com.track.task.repository.TaskRepository;
import com.track.task.service.priority.PriorityService;
import com.track.task.service.status.StatusService;
import com.track.task.service.task.TaskService;
import com.track.task.service.task.impl.TaskServiceImpl;
import com.track.task.service.user.AdminUserService;
import com.track.task.service.user.UserService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;


@Transactional
@SpringBootTest
class TaskServiceTest extends BaseIntegrationTest {

    @Autowired
    private TaskRepository taskRepository;

    @MockBean
    private UserService userService;

    @MockBean
    private PriorityService priorityService;

    @MockBean
    private StatusService statusService;

    @MockBean
    private AdminUserService adminUserService;

    private TaskService taskService;
    private User testUser;
    private Priority testPriority;
    private Status testStatus;

    @BeforeEach
    void setUp() {
        taskService = new TaskServiceImpl(taskRepository, userService, priorityService, statusService, null, adminUserService);

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");

        testPriority = new Priority();
        testPriority.setId(1L);

        testStatus = new Status();
        testStatus.setId(1L);

        when(userService.getUserByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(priorityService.getById(1L)).thenReturn(Optional.of(testPriority));
        when(statusService.getById(1L)).thenReturn(Optional.of(testStatus));
    }

    @Test
    void testCreateTask() {
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setTitle("New Task");
        taskDTO.setDescription("New Task Description");
        taskDTO.setPriorityId(1L);
        taskDTO.setStatusId(1L);
        taskDTO.setVisibility(true);
        taskDTO.setExecutorsIds(Collections.emptyList());
        taskDTO.setCommentsIds(Collections.emptyList());

        Task createdTask = taskService.createTask(taskDTO, "test@example.com");

        assertThat(createdTask).isNotNull();
        assertThat(createdTask.getTitle()).isEqualTo("New Task");
        assertThat(createdTask.getDescription()).isEqualTo("New Task Description");
        assertThat(createdTask.getPriority()).isEqualTo(testPriority);
        assertThat(createdTask.getStatus()).isEqualTo(testStatus);
    }

    @Test
    void testGetTaskById() {
        Task task = new Task();
        task.setTitle("Existing Task");
        task.setDescription("Existing Description");
        task.setUser(testUser);
        task.setPriority(testPriority);
        task.setStatus(testStatus);
        task.setVisibility(true);
        Task savedTask = taskRepository.save(task);

        Task fetchedTask = taskService.getTaskById(savedTask.getId(), "test@example.com");

        assertThat(fetchedTask).isNotNull();
        assertThat(fetchedTask.getTitle()).isEqualTo("Existing Task");
    }

    @Test
    void testUpdateTask() {
        Task task = new Task();
        task.setTitle("Old Title");
        task.setDescription("Old Description");
        task.setUser(testUser);
        task.setPriority(testPriority);
        task.setStatus(testStatus);
        task.setVisibility(true);
        Task savedTask = taskRepository.save(task);

        TaskDTO updateDto = new TaskDTO();
        updateDto.setTitle("Updated Title");
        updateDto.setDescription("Updated Description");
        updateDto.setPriorityId(1L);
        updateDto.setStatusId(1L);
        updateDto.setVisibility(true);
        updateDto.setCommentsIds(Collections.emptyList());
        updateDto.setExecutorsIds(Collections.emptyList());

        taskService.updateTask(savedTask.getId(), updateDto, "test@example.com");

        Task updatedTask = taskRepository.findById(savedTask.getId()).orElseThrow();
        assertThat(updatedTask.getTitle()).isEqualTo("Updated Title");
        assertThat(updatedTask.getDescription()).isEqualTo("Updated Description");
    }

    @Test
    void testDeleteTask() {
        Task task = new Task();
        task.setTitle("Task to delete");
        task.setDescription("Description to delete");
        task.setUser(testUser);
        task.setPriority(testPriority);
        task.setStatus(testStatus);
        task.setVisibility(true);
        Task savedTask = taskRepository.save(task);

        taskService.deleteTask(savedTask.getId(), "test@example.com");

        assertThat(taskRepository.findById(savedTask.getId())).isEmpty();
    }

    @Test
    void testUpdateTaskStatus() {
        Task task = new Task();
        task.setTitle("Task to update status");
        task.setDescription("Initial description");
        task.setUser(testUser);
        task.setPriority(testPriority);
        task.setStatus(testStatus);
        task.setVisibility(true);
        Task savedTask = taskRepository.save(task);

        Status newStatus = new Status();
        newStatus.setId(2L);
        when(statusService.getById(2L)).thenReturn(Optional.of(newStatus));

        StatusDTO statusDTO = new StatusDTO();
        statusDTO.setId(2L);

        taskService.updateTaskStatus(savedTask.getId(), statusDTO, "test@example.com");

        Task updatedTask = taskRepository.findById(savedTask.getId()).orElseThrow();
        assertThat(updatedTask.getStatus().getId()).isEqualTo(2L);
    }

    @Test
    void testUnauthorizedAccess() {
        Task task = new Task();
        task.setTitle("Private Task");
        task.setDescription("Sensitive data");
        task.setUser(testUser);
        task.setVisibility(false);

        // Устанавливаем обязательные поля
        task.setStatus(testStatus);
        task.setPriority(testPriority);

        Task savedTask = taskRepository.save(task);

        User anotherUser = new User();
        anotherUser.setId(2L);
        anotherUser.setEmail("another@example.com");

        when(userService.getUserByEmail("another@example.com")).thenReturn(Optional.of(anotherUser));

        // Expecting ForbiddenException to be thrown for unauthorized access
        assertThatThrownBy(() -> taskService.getTaskById(savedTask.getId(), "another@example.com"))
                .isInstanceOf(ForbiddenException.class);
    }


}
