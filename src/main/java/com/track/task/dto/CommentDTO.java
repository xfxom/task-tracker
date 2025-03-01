package com.track.task.dto;

import lombok.Data;
import java.util.List;

@Data
public class CommentDTO {
    private Long id;
    private String content;
    private Long userId;
    private Long parentId;
    private List<Long> repliesIds;
}

