package poncha.kiet.postservice.projection;

import java.time.LocalDateTime;

public interface PostOverview {
    String getId();

    String getTitle();

    LocalDateTime getCreatedAt();

    double getPrice();

    String getVehicleId();
}

