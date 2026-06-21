package com.api.tasklist.controller;

import com.api.tasklist.dto.TaskRequest;
import com.api.tasklist.entity.Task;
import com.api.tasklist.entity.User;
import com.api.tasklist.repository.TaskRepository;
import com.api.tasklist.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Autowired
    public TaskController(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<Task> addTask(@RequestBody TaskRequest req) {
        logger.info("Add task={} for loginId={}", req.getTaskName(), req.getLoginId());
        if (req.getLoginId() == null || req.getTaskName() == null) {
            logger.debug("Loginid or taskname is null");
            return ResponseEntity.badRequest().build();
        }

        User user = userRepository.findByLoginId(req.getLoginId())
                .orElse(null);

        if (user == null) {
            logger.warn("Attempt to add task for an unknown loginid={}", req.getLoginId());
            return ResponseEntity.badRequest().build();
        }

        Task task = new Task(req.getTaskName(), user);
        Task saved = taskRepository.save(task);
        logger.info("Task created with id={} for loginid={}", saved.getId(), req.getLoginId());
        return ResponseEntity.created(URI.create("/api/tasks/" + saved.getId())).body(saved);
    }

    @GetMapping("/{loginId}")
    public ResponseEntity<List<Task>> getTasksForUser(@PathVariable String loginId) {
        List<Task> tasks = taskRepository.findByUser_LoginId(loginId);
        return ResponseEntity.ok(tasks);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody TaskRequest req) {
        if (req.getTaskName() == null || req.getTaskName().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        return taskRepository.findById(id)
                .map(task -> {
                    task.setTaskName(req.getTaskName());
                    Task updated = taskRepository.save(task);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        if (taskRepository.existsById(id)) {
            taskRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}

