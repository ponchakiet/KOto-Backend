package poncha.kiet.userservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import poncha.kiet.userservice.dto.request.UserUpdateDTO;
import poncha.kiet.userservice.dto.response.*;
import poncha.kiet.userservice.service.UserService;

import java.time.LocalDateTime;
import java.util.Map;

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

    @GetMapping("/profile")
    public ResponseEntity<Response<UserProfileResponseDTO>> getProfileUser(@RequestHeader("X-UserId") String id) {
        return ResponseEntity.ok(Response.<UserProfileResponseDTO>builder()
                .success(true)
                .message("Get profile user successfully")
                .data(userService.findProfileUser(id))
                .timestamp(LocalDateTime.now())
                .build());
    }

    @PutMapping("/{userId}")
    public ResponseEntity<Response<UserResponseDTO>> updateProfile(
            @PathVariable("userId") String userId,
            @RequestBody UserUpdateDTO request
    ) {
        return ResponseEntity.ok(Response.<UserResponseDTO>builder()
                .success(true)
                .message("Update user successfully")
                .data(userService.updateUser(userId, request))
                .timestamp(LocalDateTime.now())
                .build());
    }

    @PutMapping("/{userId}/last-access")
    public ResponseEntity<Response<Void>> updateLastAccess(
            @PathVariable("userId") String id
    ) {
        userService.updateLastAccess(id);
        return ResponseEntity.ok(Response.<Void>builder()
                .success(true)
                .message("Update last access successfully")
                .timestamp(LocalDateTime.now())
                .build());
    }

    @PostMapping(value = "/{userId}/update-avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Response<Map<String, String>>> updateAvatar(
            @PathVariable("userId") String id,
            @RequestParam MultipartFile file
    ) {
        return ResponseEntity.ok(Response.<Map<String, String>>builder()
                .success(true)
                .message("Update avatar successfully")
                .data(userService.updateAvatar(id, file))
                .timestamp(LocalDateTime.now())
                .build());
    }

    @PatchMapping("/{userId}/deactivate")
    public ResponseEntity<Response<Void>> inActiveUser(
            @PathVariable("userId") String userId
    ) {
        userService.inActiveUser(userId);
        return ResponseEntity.ok(Response.<Void>builder()
                .success(true)
                .message("Inactive user successfully")
                .timestamp(LocalDateTime.now())
                .build());
    }

    @GetMapping("/{userId}/followers")
    public ResponseEntity<Response<PageResponse<FollowerResponse>>> getFollowers(
            @PathVariable("userId") String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(Response.<PageResponse<FollowerResponse>>builder()
                .success(true)
                .message("Get follower successfully")
                .data(PageResponse.build(userService.getFollowers(id, PageRequest.of(page, size))))
                .timestamp(LocalDateTime.now())
                .build());
    }

    @GetMapping("/{userId}/following")
    public ResponseEntity<Response<PageResponse<FollowerResponse>>> getFollowing(
            @PathVariable("userId") String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(Response.<PageResponse<FollowerResponse>>builder()
                .success(true)
                .message("Get follower successfully")
                .data(PageResponse.build(userService.getFollowing(id, PageRequest.of(page, size))))
                .timestamp(LocalDateTime.now())
                .build());
    }

    @PostMapping("/{userId}/follow/{followerId}")
    public ResponseEntity<Response<FollowerResponse>> followUser(
            @PathVariable("userId") String id,
            @PathVariable("followerId") String followerId
    ) {
        return ResponseEntity.ok(Response.<FollowerResponse>builder()
                .success(true)
                .message("Follow user successfully")
                .data(userService.followUser(id, followerId))
                .timestamp(LocalDateTime.now())
                .build());
    }

    @DeleteMapping("/{userId}/unfollow/{followerId}")
    public ResponseEntity<Response<Void>> unfollowUser(
            @PathVariable("userId") String id,
            @PathVariable("followerId") String followerId
    ) {
        userService.unfollowUser(id, followerId);
        return ResponseEntity.ok(Response.<Void>builder()
                .success(true)
                .message("Unfollow user successfully")
                .timestamp(LocalDateTime.now())
                .build());
    }
}
