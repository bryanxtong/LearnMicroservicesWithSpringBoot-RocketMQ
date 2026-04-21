package microservices.book.logs;

import org.apache.rocketmq.client.apis.consumer.ConsumeResult;
import org.apache.rocketmq.client.apis.message.MessageId;
import org.apache.rocketmq.client.apis.message.MessageView;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LogsConsumerTest {

    private final LogsConsumer logsConsumer = new LogsConsumer();

    @Test
    void consumesMessageWhenLevelIsMissing() {
        MessageView messageView = messageView(Map.of("applicationId", "gateway"), "hello");
        ConsumeResult result = logsConsumer.consume(messageView);
        assertThat(result).isEqualTo(ConsumeResult.FAILURE);
    }

    @Test
    void consumesMessageWhenLevelIsUnknown() {
        MessageView messageView = messageView(Map.of("applicationId", "gateway", "level", "DEBUG"), "hello");
        ConsumeResult result = logsConsumer.consume(messageView);
        assertThat(result).isEqualTo(ConsumeResult.SUCCESS);
    }

    private MessageView messageView(Map<String, String> properties, String body) {
        MessageView messageView = mock(MessageView.class);
        MessageId messageId = mock(MessageId.class);
        when(messageView.getProperties()).thenReturn(properties);
        when(messageView.getBody()).thenReturn(ByteBuffer.wrap(body.getBytes(StandardCharsets.UTF_8)));
        when(messageView.getMessageId()).thenReturn(messageId);
        return messageView;
    }
}
