package com.track.task.service.status.impl;

import com.track.task.dto.StatusDTO;
import com.track.task.exception.ForbiddenException;
import com.track.task.exception.NotFoundException;
import com.track.task.repository.StatusRepository;
import com.track.task.service.status.StatusService;
import com.track.task.service.user.AdminUserService;
import com.track.task.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.track.task.model.Status;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatusServiceImpl implements StatusService {

    private final StatusRepository statusRepository;
    private final UserService userService;
    private final AdminUserService adminUserService;

    @Override
    public List<Status> getAll() {
        return statusRepository.findAll();
    }

    @Override
    public Optional<Status> getById(Long id) {
        return statusRepository.findById(id);
    }

    @Override
    public Status add(StatusDTO statusDTO, String email) {

        Status status = new Status();

        status.setTitle(statusDTO.getTitle());
        status.setIsGlobal(false);

        if (adminUserService.isAdminByEmail(email)) {
            if (statusDTO.getUserId() != null && statusDTO.getIsGlobal() != null) {
                status.setUser(
                        userService.getUserById(statusDTO.getUserId())
                                .orElseThrow(() -> new NotFoundException("User not found"))
                );
                status.setIsGlobal(statusDTO.getIsGlobal());
            }
        }

        statusRepository.save(status);
        return status;
    }

    @Override
    public Status update(Long statusId, StatusDTO statusDTO) {

        Status status = getById(statusId)
                .orElseThrow(() -> new NotFoundException("Status not found"));

        status.setTitle(statusDTO.getTitle());

        statusRepository.save(status);
        return status;

    }

    @Override
    public void delete(Long statusId, String email) throws ForbiddenException {

        Status status = getById(statusId)
                .orElseThrow(() -> new NotFoundException("Status not found"));

        if (adminUserService.isAdminByEmail(email)) {
            if (status.getUser().getId() != null && status.getIsGlobal() != null) {
                if (status.getIsGlobal()) {
                    statusRepository.deleteById(statusId);
                } else {
                    throw new ForbiddenException();
                }
            }
        }
    }
}
