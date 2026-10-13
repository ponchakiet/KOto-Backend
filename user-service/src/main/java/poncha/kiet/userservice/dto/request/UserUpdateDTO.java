package poncha.kiet.userservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateDTO {
    private String email;
    private String description;
    private String firstName;
    private String lastName;
    private LocalDate dob;
    private Boolean sex;
    private String phoneNumber;
    private String taxCode;
    private String cid;
}
