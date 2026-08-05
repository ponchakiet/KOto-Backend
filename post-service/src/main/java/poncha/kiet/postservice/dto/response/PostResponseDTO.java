package poncha.kiet.postservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class PostResponseDTO {
    private String id;
    private String title;
    private String description;
    private LocalDateTime lastUpdated;
    private String userId;
    private String status;
    private VehicleDetailResponseDTO vehicleDetail;
}
