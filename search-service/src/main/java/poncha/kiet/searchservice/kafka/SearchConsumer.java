package poncha.kiet.searchservice.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import poncha.kiet.searchservice.service.AsyncService;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchConsumer {
    private final AsyncService asyncService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "koto.public.posts",
            groupId = "koto-async"
    )
    public void syncPost(ConsumerRecord<?, ?> consumerRecord) {

        try {
            JsonNode root = objectMapper.readTree((String) consumerRecord.value());

            String op = root.path("op").asText();

            String postId;

            switch (op) {

                case "c":
                case "r":
                    postId = root.path("after").path("id").asText();

                    log.info("Create post from Debezium - {}", postId);

                    asyncService.createPost(postId);
                    break;

                case "u":
                    postId = root.path("after").path("id").asText();

                    log.info("Update post from Debezium - {}", postId);

                    asyncService.updatePost(postId);
                    break;

                case "d":
                    postId = root.path("before").path("id").asText();

                    log.info("Delete post from Debezium - {}", postId);

                    asyncService.deletePost(postId);
                    break;

                default:
                    log.warn("Unsupported Debezium operation: {}", op);
            }

        } catch (Exception e) {
            log.error(
                    "Failed to process Kafka message: {}",
                    consumerRecord.value(),
                    e
            );

            throw new RuntimeException("Failed to process Kafka message", e);
        }
    }
}
