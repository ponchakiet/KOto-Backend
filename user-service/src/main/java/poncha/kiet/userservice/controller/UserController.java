package poncha.kiet.userservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poncha.kiet.userservice.dto.response.PageResponse;
import poncha.kiet.userservice.dto.response.Response;
import poncha.kiet.userservice.dto.response.UserResponseDTO;
import poncha.kiet.userservice.service.UserService;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<Response<PageResponse<UserResponseDTO>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
            ) {
        return ResponseEntity.ok(Response.<PageResponse<UserResponseDTO>>builder()
                .success(true)
                .message("Get users successfully")
                .data(PageResponse.build(userService.findAll(PageRequest.of(page, size))))
                .timestamp(LocalDateTime.now())
                .build());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Response<UserResponseDTO>> getUserById(@PathVariable String userId) {
        return ResponseEntity.ok(Response.<UserResponseDTO>builder()
                .success(true)
                .message("Get user successfully")
                .data(userService.findById(userId))
                .timestamp(LocalDateTime.now())
                .build());
    }
}
