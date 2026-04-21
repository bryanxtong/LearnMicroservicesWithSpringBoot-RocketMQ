package microservices.book.gamification;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "app.rocketmq.listener.enabled=false")
class GamificationApplicationTests {

    @Test
    void contextLoads() {
    }

}
