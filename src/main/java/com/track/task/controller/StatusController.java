package com.track.task.controller;

import com.track.task.dto.StatusDTO;
import com.track.task.service.status.StatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/status")
@RequiredArgsConstructor
public class StatusController {

    private final StatusService statusService;

    @GetMapping

    public ResponseEntity<?> getAllStatus() {
        return ResponseEntity.ok().body(statusService.getAll());
    }

    @GetMapping("/{statusId}")
    public ResponseEntity<?> getById(@PathVariable Long statusId) {
        return ResponseEntity.ok().body(statusService.getById(statusId));
    }

    @PostMapping
    public ResponseEntity<?> addStatus(@RequestBody StatusDTO statusDTO, Principal principal) {
        return ResponseEntity.ok().body(statusService.add(statusDTO, principal.getName()));
    }

    @PutMapping("/{statusId}")
    public ResponseEntity<?> updateStatus(@PathVariable Long statusId, @RequestBody StatusDTO statusDTO) {
        return ResponseEntity.ok().body(statusService.update(statusId, statusDTO));
    }

    @DeleteMapping("/{statusId}")
    public ResponseEntity<?> deleteStatus(@PathVariable Long statusId, Principal principal) {
        statusService.delete(statusId, principal.getName());
        return ResponseEntity.noContent().build();
    }
}
