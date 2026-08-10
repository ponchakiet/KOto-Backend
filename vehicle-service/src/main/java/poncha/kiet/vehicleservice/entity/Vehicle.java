package poncha.kiet.vehicleservice.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Document(collection = "vehicle")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {
    @Id
    private String id;
    private String name;
    private String description;
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    private String province;
    private String ward;
    private String address;
    private double price;
    private int manufactureYear;
    private Map<String, String> properties;
    private String[] otherInfo;
    private String projectId;
    private String userId;
    private String categoryId;
    @Builder.Default
    private VehicleType type = VehicleType.DRAFT;
    private List<Content> contents;

    public void addContent(Content content) {
        if (contents == null)
            contents = new ArrayList<>();
    }
}
