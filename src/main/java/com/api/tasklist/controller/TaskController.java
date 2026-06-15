package com.api.tasklist.controller;

import com.api.tasklist.dto.TaskRequest;
import com.api.tasklist.entity.Task;
import com.api.tasklist.entity.User;
import com.api.tasklist.repository.TaskRepository;
import com.api.tasklist.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Autowired
    public TaskController(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<Task> addTask(@RequestBody TaskRequest req) {
        if (req.getLoginId() == null || req.getTaskName() == null) {
            return ResponseEntity.badRequest().build();
        }

        User user = userRepository.findByLoginId(req.getLoginId())
                .orElseGet(() -> {
                    User u = new User(req.getUserName(), req.getLoginId(), req.getPassword());
                    return userRepository.save(u);
                });

        Task task = new Task(req.getTaskName(), user);
        Task saved = taskRepository.save(task);
        return ResponseEntity.created(URI.create("/api/tasks/" + user.getLoginId())).body(saved);
    }

    @GetMapping("/{loginId}")
    public ResponseEntity<List<Task>> getTasksForUser(@PathVariable String loginId) {
        List<Task> tasks = taskRepository.findByUser_LoginId(loginId);
        return ResponseEntity.ok(tasks);
    }
}

