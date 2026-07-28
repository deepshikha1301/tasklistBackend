package com.api.tasklist.controller;

import com.api.tasklist.dto.TaskRequest;
import com.api.tasklist.entity.Task;
import com.api.tasklist.service.TaskService;
import com.api.tasklist.service.UserService;
import jakarta.validation.Valid;
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

    private final TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<Task> addTask(@Valid @RequestBody TaskRequest req) {
        logger.info("Add task={} for loginId={}", req.taskName(), req.loginId());
        try{
            Task created = taskService.createTask(req.loginId(), req.taskName());
            return ResponseEntity.created(URI.create("/api/tasks/" + created.getId())).body(created);
        }catch (Exception e){
            logger.error("Error creating task for loginId={}: {}", req.loginId(), e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{loginId}")
    public ResponseEntity<List<String>> getTasksForUser(@PathVariable String loginId) {
        List<String> tasks = taskService.getTasksForUser(loginId);
        return ResponseEntity.ok(tasks);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @Valid @RequestBody TaskRequest req) {
        try{
            return taskService.updateTask(id, req.taskName())
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        }catch (Exception e){
            logger.error("Error updating task with id={}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }

    }

    @DeleteMapping
    public ResponseEntity<Void> deleteTaskByLoginIdAndTaskName(
            @Valid @RequestBody TaskRequest req) {
        logger.info("Delete task={} for loginId={}", req.taskName(), req.loginId());
        try {
            if (taskService.deleteTaskByLoginIdAndTaskName(req.loginId(), req.taskName())) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error deleting task for loginId={}: {}", req.loginId(), e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}

