package com.track.task.dto;

import lombok.Data;

import java.util.List;

@Data
public class TaskDTO {
    private Long id;
    private String title;
    private String description;
    private Boolean visibility;
    private Long statusId;
    private Long priorityId;
    private Long userId;
    private List<Long> executorsIds;
    private List<Long> commentsIds;
}
