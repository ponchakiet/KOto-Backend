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
//    @KafkaListener(topics = "order-topic", groupId = "saga-service")
//    public void consumeEventAndPublishTopic(List<OrderEvent> orderEvents) {
//        System.out.println("consume");
    //        Flux.fromIterable(orderEvents)
    //                .flatMap(this::processPayment)
    //                .subscribe();
}
//    private Mono<PaymentEvent> processPayment(OrderEvent orderEvent) {
//        if (OrderStatus.ORDER_CREATED.equals(orderEvent.getOrderStatus())) {
//            return Mono.fromSupplier(() -> paymentService.newOrderEvent(orderEvent));
//        } else {
//            return Mono.fromRunnable(() -> paymentService.cancelOrderEvent(orderEvent));
//        }
//    }

}
