package com.api.tasklist.service;

import com.api.tasklist.entity.Task;
import com.api.tasklist.entity.User;
import com.api.tasklist.repository.TaskRepository;
import com.api.tasklist.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Autowired
    public TaskService(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Task createTask(String loginId, String taskName) {
        if (loginId == null || loginId.trim().isEmpty() || taskName == null || taskName.trim().isEmpty()) {
            throw new IllegalArgumentException("loginId and taskName are required");
        }

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("User not found for loginId: " + loginId));

        Task task = new Task(taskName, user);
        Task saved = taskRepository.save(task);
        logger.info("Task created with id={} for loginid={}", saved.getId(), loginId);
        return saved;
    }

    public List<String> getTasksForUser(String loginId) {
        List<Task> userTasks = taskRepository.findByUser_LoginId(loginId);
        return userTasks.stream().map(Task::getTaskName).toList();
    }

    @Transactional
    public Optional<Task> updateTask(Long id, String taskName) {
        if (taskName == null || taskName.trim().isEmpty()) {
            throw new IllegalArgumentException("taskName is required");
        }

        return taskRepository.findById(id)
                .map(task -> {
                    task.setTaskName(taskName);
                    return taskRepository.save(task);
                });
    }

    @Transactional
    public boolean deleteTaskByLoginIdAndTaskName(String loginId, String taskName) {
        return taskRepository.findByUser_LoginIdAndTaskName(loginId, taskName)
                .map(task -> {
                    taskRepository.delete(task);
                    logger.info("Task deleted: taskName={} for loginId={}", taskName, loginId);
                    return true;
                })
                .orElseGet(() -> {
                    logger.warn("Task not found: taskName={} for loginId={}", taskName, loginId);
                    return false;
                });
    }
}