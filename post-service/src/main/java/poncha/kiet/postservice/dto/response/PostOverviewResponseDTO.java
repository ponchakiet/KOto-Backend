package poncha.kiet.postservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostOverviewResponseDTO {
    private String id;
    private String title;
    private double price;
    private LocalDateTime createdAt;
    private String posterUrl;
}
