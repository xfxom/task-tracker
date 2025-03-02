package com.track.task.utils;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PaginationUtils {

    public static <T> Page<T> toPage(List<T> list, Pageable pageable) {
        if (list == null) {
            throw new IllegalArgumentException("List cannot be null");
        }

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), list.size());
        List<T> sublist = list.subList(start, end);

        return new PageImpl<>(sublist, pageable, list.size());
    }
}
