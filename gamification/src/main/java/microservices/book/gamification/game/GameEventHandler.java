package microservices.book.gamification.game;

import microservices.book.event.challenge.ChallengeSolvedEvent;
import org.apache.rocketmq.client.annotation.RocketMQMessageListener;
import org.apache.rocketmq.client.apis.consumer.ConsumeResult;
import org.apache.rocketmq.client.apis.message.MessageView;
import org.apache.rocketmq.client.core.RocketMQListener;
import org.apache.rocketmq.shaded.com.google.gson.Gson;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
@Slf4j
@Service
@ConditionalOnProperty(name = "app.rocketmq.listener.enabled", havingValue = "true", matchIfMissing = true)
@RocketMQMessageListener(consumerGroup = "gamification", topic = "${rocketmq.attempts}")
public class GameEventHandler implements RocketMQListener {
    private final GameService gameService;
    private final Gson gson = new Gson();

    @Override
    public ConsumeResult consume(MessageView messageView) {
        try {
            String event = StandardCharsets.UTF_8.decode(messageView.getBody()).toString();
            ChallengeSolvedEvent challengeSolvedEvent = gson.fromJson(event, ChallengeSolvedEvent.class);
            log.info("Challenge Solved Event received: {}", challengeSolvedEvent.getAttemptId());
            gameService.newAttemptForUser(challengeSolvedEvent);
        } catch (final Exception e) {
            log.error("Error when trying to process ChallengeSolvedEvent", e);
            return ConsumeResult.FAILURE;
        }
        return ConsumeResult.SUCCESS;
    }
}