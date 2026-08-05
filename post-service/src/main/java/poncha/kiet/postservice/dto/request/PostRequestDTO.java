package poncha.kiet.postservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostRequestDTO {
    private String title;
    private String description;
    private LocalDateTime lastUpdated;
    private String vehicleId;
    private String userId;
}
