package poncha.kiet.vehicleservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Video {
    private String id;
    private String url;
    private String name;
    private LocalDateTime createdAt;
    private double duration;

}
