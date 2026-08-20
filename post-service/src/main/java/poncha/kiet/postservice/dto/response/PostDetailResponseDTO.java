package poncha.kiet.postservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import poncha.kiet.postservice.dto.response.UserPostInfoResponseDTO;
import poncha.kiet.postservice.dto.response.VehicleDetailResponseDTO;

import java.time.LocalDateTime;
import java.util.List;

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