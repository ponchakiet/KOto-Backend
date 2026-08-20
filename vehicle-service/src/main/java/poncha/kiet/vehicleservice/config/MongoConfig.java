package poncha.kiet.vehicleservice.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class MongoConfig {

    @Bean
    public MongoClient mongoClient(
            @Value("${spring.data.mongodb.host}") String host,
            @Value("${spring.data.mongodb.port}") int port
    ) {
        return MongoClients.create(
                "mongodb://" + host + ":" + port
        );
    }

    @Bean
    public MongoTemplate mongoTemplate(
            MongoClient mongoClient,
            @Value("${spring.data.mongodb.database}") String database
    ) {
        return new MongoTemplate(mongoClient, database);
    }
}