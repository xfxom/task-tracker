package com.track.task.service.status;


import com.track.task.dto.StatusDTO;
import com.track.task.exception.ForbiddenException;
import com.track.task.model.Status;

import java.util.List;
import java.util.Optional;


public interface StatusService {
    List<Status> getAll();
    Optional<Status> getById(Long id);
    Status add(StatusDTO statusDTO, String email);
    Status update(Long statusId, StatusDTO statusDTO);
    void delete(Long statusId, String email) throws ForbiddenException;
}
