package com.paralegal.paralegalApp;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Disabled("Disable full-context smoke test for now")
class ParalegalAppApplicationTests {

	@Test
	void contextLoads() {
	}

}
