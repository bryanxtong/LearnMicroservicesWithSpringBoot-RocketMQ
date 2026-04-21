package microservices.book.logs;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "app.rocketmq.listener.enabled=false")
class LogsApplicationTests {

	@Test
	void contextLoads() {
	}

}
