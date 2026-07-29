package com.api.tasklist.service;

import com.api.tasklist.entity.Task;
import com.api.tasklist.entity.User;
import com.api.tasklist.exception.NoSuchUserException;
import com.api.tasklist.repository.TaskRepository;
import com.api.tasklist.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    TaskRepository taskRepository;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    TaskService taskService;

    @Test
    void shouldCreateTaskSuccessfully(){
        Task task = createTask();
        User user = createUser();
        Mockito.when(userRepository.findByLoginId(anyString())).thenReturn(Optional.of(user));
        Mockito.when(taskRepository.save(any())).thenReturn(task);

        Task output = taskService.createTask("abc_xyz", "do something");

        Assertions.assertNotNull(task);
        Assertions.assertEquals(task.getTaskName(), output.getTaskName());
    }

    @Test
    void shouldThrowExceptionUserNotFound(){
        assertThrows(NoSuchUserException.class, ()->{
            taskService.createTask("abc_xyz", "do something");
        });
    }

    @Test
    void shouldGetTasksSuccessfully(){
        Mockito.when(taskRepository.findByUser_LoginId(anyString())).thenReturn(createTaskList());
        List<String> list = taskService.getTasksForUser("abc_xyz");
        assertEquals(1, list.size());
    }

    @Test
    void shouldUpdateTaskSuccessfully(){
        Task task = createTask();
        Mockito.when(taskRepository.findById(anyLong())).thenReturn(Optional.of(task));
        Mockito.when(taskRepository.save(any(Task.class))).thenReturn(task);
        Optional<Task> output = taskService.updateTask(1l, "do something");
        Assertions.assertTrue(output.isPresent());
    }

    @Test
    void updateTask_shouldThrowIllegalArgumentException(){
        assertThrows(IllegalArgumentException.class, ()->{
            taskService.updateTask(1L, "   ");
        });
    }

    @Test
    void shouldDeleteTaskSuccessfully(){
        Task task = createTask();
        Mockito.when(taskRepository.findByUser_LoginIdAndTaskName(anyString(), anyString()))
                .thenReturn(Optional.of(task));

        boolean result = taskService.deleteTaskByLoginIdAndTaskName("abc_xyz", "do something");

        assertTrue(result);
        Mockito.verify(taskRepository, Mockito.times(1)).delete(task);
    }

    @Test
    void shouldReturnFalseWhenTaskNotFound(){
        Mockito.when(taskRepository.findByUser_LoginIdAndTaskName(anyString(), anyString()))
                .thenReturn(Optional.empty());

        boolean result = taskService.deleteTaskByLoginIdAndTaskName("abc_xyz", "non_existent_task");

        assertFalse(result);
        Mockito.verify(taskRepository, Mockito.never()).delete(any());
    }

    private Task createTask(){
        Task task = new Task();
        task.setTaskName("do something");
        task.setId(1L);
        task.setUser(createUser());
        return task;
    }

    private User createUser(){
        User user = new User();
        user.setId(1L);
        user.setEmail("abcxyz@gmail.com");
        user.setPassword("password");
        user.setLoginId("abc_xyz");
        return user;
    }

    private List<Task> createTaskList(){
        return List.of(createTask());
    }
}