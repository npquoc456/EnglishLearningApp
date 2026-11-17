package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "APP_JWT_SECRET=TEST_ONLY_NEVER_DEPLOY_0123456789_0123456789")
class DemoApplicationTests {

	@Test
	void contextLoads() {
	}

}
