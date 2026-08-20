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
public class PostDetailResponseDTO {
    private String id;
    private String title;
    private String description;
    private String createdAt;
    private double price;
    private String type;
    private LocalDateTime lastUpdated;
    private String status;
    private VehicleDetailResponseDTO vehicleDetail;
    private UserPostInfoResponseDTO userInfo;
}