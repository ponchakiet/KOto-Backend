package poncha.kiet.postservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class UserPostInfoResponseDTO {
    private String id;
    private String firstName;
    private String lastName;
    private String avtUrl;
    private String email;
    private String phoneNumber;
    private LocalDateTime createdAt;
    private LocalDateTime lastAccess;
}