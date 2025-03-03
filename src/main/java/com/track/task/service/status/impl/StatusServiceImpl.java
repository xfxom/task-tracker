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
        log.info("Fetching all statuses");
        List<Status> statuses = statusRepository.findAll();
        log.debug("Found {} statuses", statuses.size());
        return statuses;
    }

    @Override
    public Optional<Status> getById(Long id) {
        log.info("Fetching status by id: {}", id);
        Optional<Status> status = statusRepository.findById(id);
        if (status.isPresent()) {
            log.debug("Status with id: {} found", id);
        } else {
            log.warn("Status with id: {} not found", id);
        }
        return status;
    }

    @Override
    public Status add(StatusDTO statusDTO, String email) {
        log.info("Creating new status. Requested by: {}", email);
        Status status = new Status();
        status.setTitle(statusDTO.getTitle());
        status.setIsGlobal(false);
        log.debug("Initial status setup: {}", status);

        if (adminUserService.isAdminByEmail(email)) {
            log.debug("Admin privileges detected for user: {}", email);
            if (statusDTO.getUserId() != null && statusDTO.getIsGlobal() != null) {
                log.debug("Processing admin-specific settings");
                status.setUser(
                        userService.getUserById(statusDTO.getUserId())
                                .orElseThrow(() -> {
                                    log.error("User not found with id: {}", statusDTO.getUserId());
                                    return new NotFoundException("User not found");
                                })
                );
                status.setIsGlobal(statusDTO.getIsGlobal());
                log.debug("Set user: {} and global flag: {}", statusDTO.getUserId(), statusDTO.getIsGlobal());
            }
        }

        Status savedStatus = statusRepository.save(status);
        log.info("Successfully created status with id: {}", savedStatus.getId());
        return savedStatus;
    }

    @Override
    public Status update(Long statusId, StatusDTO statusDTO) {
        log.info("Updating status with id: {}", statusId);
        Status status = getById(statusId)
                .orElseThrow(() -> {
                    log.error("Status not found for update: {}", statusId);
                    return new NotFoundException("Status not found");
                });

        log.debug("Updating title from '{}' to '{}'", status.getTitle(), statusDTO.getTitle());
        status.setTitle(statusDTO.getTitle());

        Status updatedStatus = statusRepository.save(status);
        log.info("Successfully updated status with id: {}", statusId);
        return updatedStatus;
    }

    @Override
    public void delete(Long statusId, String email) throws ForbiddenException {
        log.info("Attempting to delete status: {} by user: {}", statusId, email);
        Status status = getById(statusId)
                .orElseThrow(() -> {
                    log.error("Status not found for deletion: {}", statusId);
                    return new NotFoundException("Status not found");
                });

        log.debug("Fetched status with id: {} and isGlobal: {}", statusId, status.getIsGlobal());

        if (adminUserService.isAdminByEmail(email)) {
            log.debug("Admin delete attempt for status: {}", statusId);
            statusRepository.deleteById(statusId);
            log.info("Successfully deleted status: {}", statusId);
        } else {
            if (status.getIsGlobal() && status.getUser() != null && status.getUser().getEmail().equals(email)) {
                statusRepository.deleteById(statusId);
                log.info("Successfully deleted global status by author: {}", statusId);
            } else {
                log.warn("Unauthorized delete attempt by non-author user: {} for status: {}", email, statusId);
                throw new ForbiddenException("User is not authorized to delete status");
            }
        }
    }
}