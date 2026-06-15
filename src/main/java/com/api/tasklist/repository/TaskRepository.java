package com.api.tasklist.repository;

import com.api.tasklist.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUser_LoginId(String loginId);
}

