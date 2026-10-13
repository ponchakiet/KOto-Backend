package poncha.kiet.searchservice.component;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import poncha.kiet.searchservice.service.AsyncService;

@Component
@RequiredArgsConstructor
public class ElasticsearchStartupRunner implements CommandLineRunner {
    private final AsyncService asyncService;

    @Override
    public void run(String... args) {
        asyncService.migrateData();
    }
}
