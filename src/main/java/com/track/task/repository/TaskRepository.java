package com.track.task.repository;

import com.track.task.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    @Query("SELECT t FROM Task t WHERE t.user.email = :email")
    List<Task> findAllByByUserEmail(String email);

    List<Task> findByVisibility(Boolean visible);

    @Query("SELECT t FROM Task t WHERE t.user.email = :email AND t.visibility = :visible")
    List<Task> findAllByUserEmailAndVisibility(String email, Boolean visible);
}
