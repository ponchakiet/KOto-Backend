package poncha.kiet.userservice.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import poncha.kiet.userservice.dto.response.UserResponseDTO;

public interface UserService {
    Page<UserResponseDTO> findAll(Pageable pageable);
    UserResponseDTO findById(String id);
}
