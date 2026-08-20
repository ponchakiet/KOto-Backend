package poncha.kiet.userservice.projection;

import java.time.LocalDateTime;

public interface UserPostInfo {
    String getId();

    String getFirstName();

    String getLastName();

    String getAvtUrl();

    String getEmail();

    LocalDateTime getCreatedAt();

    LocalDateTime getLastAccess();

    String getPhoneNumber();

}