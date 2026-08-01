package com.api.tasklist.repository;

import com.api.tasklist.entity.Task;
import com.api.tasklist.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class TaskRepositoryTest {

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    UserRepository userRepository;

    private Task task = null;

    @BeforeEach
    void setUp(){
        User user = mockUser();
        User savedUser = userRepository.save(user);
        task = mockTask(savedUser);
        taskRepository.save(task);
    }

    @Test
    void shouldFindTaskByUserLoginIdAndTaskName(){
        Task foundTask = taskRepository.findByUser_LoginIdAndTaskName("abc_xyz", "do something").get();
        assertEquals(task.getId(), foundTask.getId());
        assertEquals(task.getTaskName(), foundTask.getTaskName());
    }

    @Test
    void shouldFindTasksByUserLoginId(){
        List<Task> foundTasks = taskRepository.findByUser_LoginId("abc_xyz");
        assertEquals(1, foundTasks.size());
        assertEquals(task.getId(), foundTasks.get(0).getId());
    }


    private Task mockTask(User user){
        Task task = new Task();
        task.setTaskName("do something");
        task.setUser(user);
        return task;
    }

    private User mockUser(){
        User user = new User();
        user.setEmail("abcxyz@gmail.com");
        user.setPassword("password");
        user.setLoginId("abc_xyz");
        return user;
    }
}