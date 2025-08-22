package com.ipmonitoring.ipmonitoringapp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class IpmonitoringappApplicationTests {

	@Test
	@WithMockUser
	void contextLoads() {
	}
}
