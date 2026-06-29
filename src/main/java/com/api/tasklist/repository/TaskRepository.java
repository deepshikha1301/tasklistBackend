package com.api.tasklist.repository;

import com.api.tasklist.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUser_LoginId(String loginId);
    Optional<Task> findByUser_LoginIdAndTaskName(String loginId, String taskName);
}

