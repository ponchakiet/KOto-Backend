package poncha.kiet.vehicleservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import poncha.kiet.vehicleservice.entity.Content;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehicleRequestDTO {
    private String id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private String province;
    private String ward;
    private String address;
    private double price;
    private int manufactureYear;
    private Map<String, String> properties;
    private String projectId;
    private String userId;
    private List<Content> contents;
}
