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
    private LocalDateTime createdAt;
    private String province;
    private String ward;
    private String address;
    private double price;
    private int manufactureYear;
    private Map<String, String> properties;
    private String[] otherInfo;
    private String type;
    private String projectId;
    private String userId;
    private String categoryId;
    private List<Content> contents;
}
