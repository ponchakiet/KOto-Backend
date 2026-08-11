package poncha.kiet.postservice.projection;

import poncha.kiet.postservice.entity.PostStatus;

import java.time.LocalDateTime;

public interface PostHistory {
    String getId();

    String getTitle();

    PostStatus getStatus();

    LocalDateTime getCreatedAt();
}
