package poncha.kiet.userservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSignUpRequestDTO {
    private String userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String password;
}