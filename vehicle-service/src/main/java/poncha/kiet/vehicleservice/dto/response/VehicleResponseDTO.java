package poncha.kiet.vehicleservice.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import poncha.kiet.vehicleservice.entity.Content;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class VehicleResponseDTO {
    private String id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private String province;
    private String ward;
    private String address;
    private double price;
    private int manufactureYear;
    private String[] otherInfo;
    private Map<String, String> properties;
    private String projectId;
    private String userId;
    private List<Content> contents;
    private CategoryResponseDTO category;
}
