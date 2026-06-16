package com.api.tasklist;

import com.api.tasklist.controller.TaskController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class TasklistApplicationTests {

	@Autowired
	private ApplicationContext context;

	@Test
	void contextLoads() {
		// Verifies that the Spring application context starts successfully
		assertNotNull(context, "Spring application context should have been created");
		// Verify a specific bean exists in the context
		TaskController controller = context.getBean(TaskController.class);
		assertNotNull(controller, "TaskController bean should be present in the context");
	}

}
