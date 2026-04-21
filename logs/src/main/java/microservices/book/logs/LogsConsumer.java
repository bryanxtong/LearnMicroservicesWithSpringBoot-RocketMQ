package microservices.book.logs;

import org.apache.rocketmq.client.annotation.RocketMQMessageListener;
import org.apache.rocketmq.client.apis.consumer.ConsumeResult;
import org.apache.rocketmq.client.apis.message.MessageView;
import org.apache.rocketmq.client.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Service
@ConditionalOnProperty(name = "app.rocketmq.listener.enabled", havingValue = "true", matchIfMissing = true)
@RocketMQMessageListener(consumerGroup = "logs", topic = "logs", requestTimeout = 15)
public class LogsConsumer implements RocketMQListener {
    @Override
    public ConsumeResult consume(MessageView messageView) {
        Map<String, String> properties = messageView.getProperties();
        String level = properties.get("level");
        String appId = properties.get("applicationId");
        try{
            String msg = StandardCharsets.UTF_8.decode(messageView.getBody()).toString();
            if(appId == null) {
                Marker marker = MarkerFactory.getMarker("UNKNOWN");
                log.warn(marker, msg);
                return ConsumeResult.SUCCESS;
            }
            Marker marker = MarkerFactory.getMarker(appId);
            switch (level) {
                case "INFO" -> log.info(marker, msg);
                case "ERROR" -> log.error(marker, msg);
                case "WARN" -> log.warn(marker, msg);
            }
        }catch(Exception e){
            log.error("Unable to process logs for messages id " + messageView.getMessageId());
            return ConsumeResult.FAILURE;
        }

        return ConsumeResult.SUCCESS;
    }
}
