package poncha.kiet.postservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class VehicleDetailResponseDTO {
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

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Data
    public static class Content {
        private String id;
        private String name;
        private String url;
        private String type;
        private double duration;
    }
}