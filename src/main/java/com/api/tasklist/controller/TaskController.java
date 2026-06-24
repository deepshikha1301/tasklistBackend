package com.api.tasklist.controller;

import com.api.tasklist.dto.TaskRequest;
import com.api.tasklist.entity.Task;
import com.api.tasklist.service.TaskService;
import com.api.tasklist.service.UserService;
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
    public TaskController(TaskService taskService, UserService userService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<Task> addTask(@RequestBody TaskRequest req) {
        logger.info("Add task={} for loginId={}", req.getTaskName(), req.getLoginId());
        try{
            Task created = taskService.createTask(req.getLoginId(), req.getTaskName());
            return ResponseEntity.created(URI.create("/api/tasks/" + created.getId())).body(created);
        }catch (Exception e){
            logger.error("Error creating task for loginId={}: {}", req.getLoginId(), e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{loginId}")
    public ResponseEntity<List<Task>> getTasksForUser(@PathVariable String loginId) {
        List<Task> tasks = taskService.getTasksForUser(loginId);
        return ResponseEntity.ok(tasks);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody TaskRequest req) {
        try{
            return taskService.updateTask(id, req.getTaskName())
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        }catch (Exception e){
            logger.error("Error updating task with id={}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        if (taskService.deleteTask(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}

