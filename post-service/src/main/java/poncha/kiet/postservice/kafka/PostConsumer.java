package poncha.kiet.postservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import poncha.kiet.postservice.entity.PostStatus;
import poncha.kiet.postservice.entity.PurposeType;
import poncha.kiet.postservice.event.PaymentEvent;
import poncha.kiet.postservice.event.PaymentStatus;
import poncha.kiet.postservice.service.PostService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PostConsumer {
    private final PostService postService;
    private final String PAYMENT_TOPIC = "koto-payment-topic";

    @KafkaListener(topics = PAYMENT_TOPIC, groupId = "koto-post")
    public void consumeTopic(List<PaymentEvent> paymentEvents) {
        System.out.println("consume");
        Flux.fromIterable(paymentEvents)
                .filter(paymentEvent -> PurposeType.POST.name().equals(paymentEvent.getPaymentRequestDto().getPurposeType()))
                .flatMap(paymentEvent -> Mono.fromRunnable(() -> {
                    if (PaymentStatus.SUCCESS.equals(paymentEvent.getPaymentStatus()))
                        postService.updateStatusPost(paymentEvent.getPaymentRequestDto().getPurposeId(),
                                PostStatus.WAITING_ACCEPT);
                    else
                        postService.deletePost(paymentEvent.getPaymentRequestDto().getPurposeId());
                }))
                .subscribe();
    }
}
