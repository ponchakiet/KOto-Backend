package poncha.kiet.postservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class PostResponseDTO {
    private String id;
    private String title;
    private String description;
    private LocalDateTime lastUpdated;
    private String userId;
    private String status;
}
