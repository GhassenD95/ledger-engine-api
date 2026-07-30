package tn.finix.ledgerengine.scheduler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import tn.finix.ledgerengine.config.RabbitMQConfig;
import tn.finix.ledgerengine.entity.OutboxStatus;
import tn.finix.ledgerengine.entity.TransactionOutbox;
import tn.finix.ledgerengine.repository.TransactionOutboxRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxSchedulerTest {

    @Mock
    private TransactionOutboxRepository outboxRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private OutboxScheduler outboxScheduler;

    @Test
    void shouldSkipWhenNoPendingEvents() {
        when(outboxRepository.fetchPendingEvents()).thenReturn(List.of());

        outboxScheduler.processPendingOutboxEvents();

        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    void shouldPublishAndMarkProcessed() {
        var event = TransactionOutbox.builder()
                .id(UUID.randomUUID())
                .eventType("TRANSFER_COMPLETED")
                .payload("{\"amount\":100}")
                .status(OutboxStatus.PENDING)
                .createdAt(OffsetDateTime.now())
                .build();

        when(outboxRepository.fetchPendingEvents()).thenReturn(List.of(event));

        outboxScheduler.processPendingOutboxEvents();

        verify(rabbitTemplate).convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY,
                event.getPayload());
        assertEquals(OutboxStatus.PUBLISHED, event.getStatus());
        verify(outboxRepository).save(event);
    }

    @Test
    void shouldHandleRabbitMqFailureGracefully() {
        var event = TransactionOutbox.builder()
                .id(UUID.randomUUID())
                .eventType("TRANSFER_COMPLETED")
                .payload("{\"amount\":100}")
                .status(OutboxStatus.PENDING)
                .createdAt(OffsetDateTime.now())
                .build();

        when(outboxRepository.fetchPendingEvents()).thenReturn(List.of(event));
        doThrow(new RuntimeException("Connection refused"))
                .when(rabbitTemplate).convertAndSend(
                        RabbitMQConfig.EXCHANGE_NAME,
                        RabbitMQConfig.ROUTING_KEY,
                        event.getPayload());

        outboxScheduler.processPendingOutboxEvents();

        assertEquals(OutboxStatus.PENDING, event.getStatus());
        verify(outboxRepository, never()).save(event);
    }
}
