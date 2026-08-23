package poncha.kiet.userservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDTO {
    private String id;
    private String username;
    private String bannerUrl;
    private String description;
    private String email;
    private String firstName;
    private String lastName;
    private LocalDate dob;
    private LocalDateTime createdAt;
    private LocalDateTime lastAccess;
    private boolean isActive;
    private boolean sex;
    private String phoneNumber;
    private String taxCode;
    private String cid;
    private String avtUrl;
}
