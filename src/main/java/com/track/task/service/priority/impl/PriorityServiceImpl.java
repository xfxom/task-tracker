package com.track.task.service.priority.impl;

import com.track.task.repository.PriorityRepository;
import com.track.task.service.priority.PriorityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.track.task.model.Priority;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriorityServiceImpl implements PriorityService {

    private final PriorityRepository priorityRepository;

    public List<Priority> getAll() {
        log.info("Get all priority");
        return priorityRepository.findAll();
    }

    public Optional<Priority> getById(Long id) {
        log.info("Find priority by id: " + id);
        return priorityRepository.findById(id);
    }
}
