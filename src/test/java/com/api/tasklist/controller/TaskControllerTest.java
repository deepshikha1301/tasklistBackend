package com.api.tasklist.controller;

import com.api.tasklist.dto.TaskRequest;
import com.api.tasklist.entity.Task;
import com.api.tasklist.entity.User;
import com.api.tasklist.filter.JwtFilter;
import com.api.tasklist.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
class TaskControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    TaskService taskService;

    @MockitoBean
    JwtFilter jwtFilter;

    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldAddTaskSuccessfully() throws Exception {
        Task task = mockTask();
        TaskRequest req = mockRequest();
        Mockito.when(taskService.createTask(anyString(), anyString())).thenReturn(task);

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/tasks/1"));
    }

    @Test
    void shouldGetTasksForUserSuccessfully() throws Exception {
        List<String> tasks = List.of(mockTask().getTaskName());
        Mockito.when(taskService.getTasksForUser(anyString())).thenReturn(tasks);

        mockMvc.perform(get("/api/tasks/{loginId}", "abc_xyz")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void shouldUpdateTaskSuccessfully() throws Exception {
        Task task = mockTask();
        TaskRequest req = mockRequest();
        Mockito.when(taskService.updateTask(anyLong(), any())).thenReturn(Optional.of(task));

        mockMvc.perform(put("/api/tasks/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskName").value( "do something"));
    }

    @Test
    void shouldDeleteTaskSuccessfully() throws Exception {
        Mockito.when(taskService.deleteTaskByLoginIdAndTaskName(anyString(), anyString())).thenReturn(true);

        mockMvc.perform(delete("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockRequest())))
                .andExpect(status().isNoContent());

    }

    private Task mockTask(){
        Task task = new Task();
        task.setTaskName("do something");
        task.setId(1L);
        task.setUser(mockUser());
        return task;
    }

    private User mockUser(){
        User user = new User();
        user.setId(1L);
        user.setEmail("abcxyz@gmail.com");
        user.setPassword("password");
        user.setLoginId("abc_xyz");
        return user;
    }

    private TaskRequest mockRequest(){
        return new TaskRequest("abc_xyz", "do something");
    }
}