package poncha.kiet.userservice.projection;

import java.time.LocalDateTime;

public interface UserLandingPage {
    String getId();

    String getDescription();

    LocalDateTime getCreatedAt();

    String getFirstName();

    String getLastName();

    String getAvtUrl();

    String getBannerUrl();


}