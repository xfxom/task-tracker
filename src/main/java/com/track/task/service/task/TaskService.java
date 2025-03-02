package com.track.task.service.task;


import com.track.task.dto.StatusDTO;
import com.track.task.dto.TaskDTO;
import com.track.task.exception.ForbiddenException;
import com.track.task.model.Task;

import java.util.List;

public interface TaskService {
    Task createTask(TaskDTO taskDto, String email);
    List<Task> getTasks(String email, Boolean visible);
    Task getTaskById(Long taskId, String userEmail) throws ForbiddenException;
    void updateTask(Long taskId, TaskDTO taskDto, String userEmail) throws ForbiddenException;
    void deleteTask(Long taskId, String userEmail) throws ForbiddenException;
    Task updateTaskStatus(Long taskId, StatusDTO statusDto, String userEmail) throws ForbiddenException;
}
