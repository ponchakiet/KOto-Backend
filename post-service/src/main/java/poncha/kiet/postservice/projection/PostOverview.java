package poncha.kiet.postservice.projection;

import poncha.kiet.postservice.entity.PostStatus;
import poncha.kiet.postservice.entity.PostType;

import java.time.LocalDateTime;

public interface PostOverview {
    String getId();

    String getTitle();

    LocalDateTime getCreatedAt();

    double getPrice();

    String getVehicleId();

    String getUserId();

    PostStatus getStatus();

    PostType getType();
}

