package poncha.kiet.paymentservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import poncha.kiet.paymentservice.event.PaymentEvent;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentPublisher {
    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;
    private final String TOPIC = "koto-payment-topic";

    public void publishMessage(PaymentEvent paymentEvent) {
        log.info("🔥 ĐANG PUBLISH PAYMENT EVENT: {}", paymentEvent);

        kafkaTemplate.send(
                TOPIC,
                String.valueOf(paymentEvent.getPaymentRequestDto().getPurposeId()),
                paymentEvent
        ).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("❌ Kafka publish FAILED", ex);
            } else {
                log.info("✅ Kafka publish SUCCESS: topic={}, offset={}",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().offset());
            }
        });

    }
}
