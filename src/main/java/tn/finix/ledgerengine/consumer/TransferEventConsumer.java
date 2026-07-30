package tn.finix.ledgerengine.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tn.finix.ledgerengine.config.RabbitMQConfig;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransferEventConsumer {

    private final ObjectMapper objectMapper;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void handleTransferCompleted(String payload) {
        try {
            Map<String, Object> data = objectMapper.readValue(payload, Map.class);
            log.info("Received TRANSFER_COMPLETED event: ref={}, amount={}, currency={}",
                    data.get("referenceId"), data.get("amount"), data.get("currency"));
        } catch (Exception e) {
            log.error("Failed to process transfer event: {}", e.getMessage());
        }
    }
}
