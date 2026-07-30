package tn.finix.ledgerengine.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tn.finix.ledgerengine.config.RabbitMQConfig;
import tn.finix.ledgerengine.entity.OutboxStatus;
import tn.finix.ledgerengine.entity.TransactionOutbox;
import tn.finix.ledgerengine.repository.TransactionOutboxRepository;

import java.time.OffsetDateTime;
import java.util.List;

@Component
@ConditionalOnProperty(name = "app.rabbitmq.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class OutboxScheduler {

    private final TransactionOutboxRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;

    @Scheduled(fixedDelay = 3000)
    @Transactional
    public void processPendingOutboxEvents() {
        List<TransactionOutbox> pendingEvents = outboxRepository.fetchPendingEvents();

        if (pendingEvents.isEmpty()) {
            return;
        }

        log.info("Found {} PENDING outbox events to publish to RabbitMQ.", pendingEvents.size());

        for (TransactionOutbox event : pendingEvents) {
            try {
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.EXCHANGE_NAME,
                        RabbitMQConfig.ROUTING_KEY,
                        event.getPayload()
                );

                event.setStatus(OutboxStatus.PUBLISHED);
                event.setProcessedAt(OffsetDateTime.now());
                outboxRepository.save(event);

                log.info("Successfully published outbox event ID: {} [Type: {}]", event.getId(), event.getEventType());

            } catch (Exception e) {
                log.error("Failed to publish outbox event ID: {}. Reason: {}", event.getId(), e.getMessage());
            }
        }
    }
}
