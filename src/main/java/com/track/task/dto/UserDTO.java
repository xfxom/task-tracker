package com.track.task.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserDTO {
    private Long id;
    private String email;
    private List<RoleDTO> roles;
}