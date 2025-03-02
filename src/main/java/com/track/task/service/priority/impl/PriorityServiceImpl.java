package com.track.task.service.priority.impl;

import com.track.task.repository.PriorityRepository;
import com.track.task.service.priority.PriorityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.track.task.model.Priority;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class PriorityServiceImpl implements PriorityService {

    private final PriorityRepository priorityRepository;

    public List<Priority> getAll() {
        return priorityRepository.findAll();
    }

    public Optional<Priority> getById(Long id) {
        return priorityRepository.findById(id);
    }
}
