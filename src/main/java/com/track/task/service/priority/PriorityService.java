package com.track.task.service.priority;


import com.track.task.model.Priority;

import java.util.List;
import java.util.Optional;

public interface PriorityService {
    List<Priority> getAll();
    Optional<Priority> getById(Long id);

}
