package poncha.kiet.paymentservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import poncha.kiet.paymentservice.event.OrderEvent;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentConsumer {
    @KafkaListener(topics = "order-topic", groupId = "saga-service")
    public void consumeEventAndPublishTopic(List<OrderEvent> orderEvents) {
        System.out.println("consume");
    }
}
