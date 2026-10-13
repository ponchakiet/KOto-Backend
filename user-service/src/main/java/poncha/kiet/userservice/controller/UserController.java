package poncha.kiet.userservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import poncha.kiet.userservice.dto.request.UserSignUpRequestDTO;
import poncha.kiet.userservice.dto.request.UserUpdateDTO;
import poncha.kiet.userservice.dto.response.*;
import poncha.kiet.userservice.service.UserService;
import poncha.kiet.userservice.service.impl.KeycloakService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final KeycloakService keycloakService;

    @PostMapping("/users/sign-up")
    public ResponseEntity<Response<UserSignUpRequestDTO>> signUpUser(@RequestBody UserSignUpRequestDTO request) {
        keycloakService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Response.<UserSignUpRequestDTO>builder()
                .success(true)
                .message("Sign up user successfully")
                .data(userService.createUser(request))
                .timestamp(LocalDateTime.now())
                .build());
    }

    @GetMapping("/public/users/page/{userId}")
    public ResponseEntity<Response<UserLandingPageResponseDTO>> getUserLandingPage(@PathVariable("userId") String userId) {
        return ResponseEntity.ok(Response.<UserLandingPageResponseDTO>builder()
                .success(true)
                .message("Get user landing page successfully")
                .data(userService.findUserLandingPage(userId))
                .timestamp(LocalDateTime.now())
                .build());
    }

    @GetMapping("/users")
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

    @GetMapping("/user")
    public ResponseEntity<Response<UserResponseDTO>> getUserById(@RequestHeader("X-UserId") String userId) {
        return ResponseEntity.ok(Response.<UserResponseDTO>builder()
                .success(true)
                .message("Get user successfully")
                .data(userService.findById(userId))
                .timestamp(LocalDateTime.now())
                .build());
    }

    @GetMapping("/users/profile")
    public ResponseEntity<Response<UserProfileResponseDTO>> getProfileUser(
            HttpServletRequest httpServletRequest,
            @RequestHeader("X-UserId") String id) {
        return ResponseEntity.ok(Response.<UserProfileResponseDTO>builder()
                .success(true)
                .message("Get profile user successfully")
                .data(userService.findProfileUser(id, httpServletRequest))
                .timestamp(LocalDateTime.now())
                .build());
    }

    @PutMapping("/users")
    public ResponseEntity<Response<UserResponseDTO>> updateProfile(
            @RequestHeader("X-UserId") String userId,
            @RequestBody UserUpdateDTO request
    ) {
        return ResponseEntity.ok(Response.<UserResponseDTO>builder()
                .success(true)
                .message("Update user successfully")
                .data(userService.updateUser(userId, request))
                .timestamp(LocalDateTime.now())
                .build());
    }

    @PutMapping("/users/{userId}/last-access")
    public ResponseEntity<Response<Void>> updateLastAccess(
            @RequestHeader("X-UserId") String id) {
        userService.updateLastAccess(id);
        return ResponseEntity.ok(Response.<Void>builder()
                .success(true)
                .message("Update last access successfully")
                .timestamp(LocalDateTime.now())
                .build());
    }

    @PostMapping(value = "/users/upload-media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Response<Map<String, String>>> updateMedia(
            @RequestHeader("X-UserId") String userId,
            @RequestParam(name = "type") String type,
            @RequestParam MultipartFile file
    ) {
        return ResponseEntity.ok(Response.<Map<String, String>>builder()
                .success(true)
                .message("Update avatar successfully")
                .data(userService.updateMedia(userId, type, file))
                .timestamp(LocalDateTime.now())
                .build());
    }

    @PatchMapping("/users/{userId}/deactivate")
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

    @GetMapping("/users/followers")
    public ResponseEntity<Response<PageResponse<FollowerResponse>>> getFollowers(
            @RequestHeader("X-UserId") String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(Response.<PageResponse<FollowerResponse>>builder()
                .success(true)
                .message("Get follower successfully")
                .data(PageResponse.build(userService.getFollowers(id, PageRequest.of(page, size))))
                .timestamp(LocalDateTime.now())
                .build());
    }

    @GetMapping("/users/following")
    public ResponseEntity<Response<PageResponse<FollowerResponse>>> getFollowing(
            @RequestHeader("X-UserId") String id,
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

    @PostMapping("/users/follow/{followerId}")
    public ResponseEntity<Response<Map<String, String>>> followUser(
            @RequestHeader("X-UserId") String id,
            @PathVariable("followerId") String followerId
    ) {
        return ResponseEntity.ok(Response.<Map<String, String>>builder()
                .success(true)
                .message("Follow user successfully")
                .data(userService.followUser(id, followerId))
                .timestamp(LocalDateTime.now())
                .build());
    }

    @GetMapping("/users/follow/{followerId}")
    public ResponseEntity<Response<Map<String, String>>> checkUserFollow(
            @RequestHeader("X-UserId") String id,
            @PathVariable("followerId") String followerId) {
        return ResponseEntity.ok(Response.<Map<String, String>>builder()
                .success(true)
                .message("Check user follow successfully")
                .data(userService.checkUserFollow(id, followerId))
                .timestamp(LocalDateTime.now())
                .build());
    }

    @DeleteMapping("/users/unfollow/{followerId}")
    public ResponseEntity<Response<Map<String, String>>> unfollowUser(
            @RequestHeader("X-UserId") String id,
            @PathVariable("followerId") String followerId
    ) {
        return ResponseEntity.ok(Response.<Map<String, String>>builder()
                .success(true)
                .message("Unfollow user successfully")
                .data(userService.unfollowUser(id, followerId))
                .timestamp(LocalDateTime.now())
                .build());
    }

    @GetMapping("/users/count")
    public ResponseEntity<Response<?>> userCount(
            @RequestHeader("X-Roles") List<String> roles,
            @RequestParam LocalDate fromDate,
            @RequestParam LocalDate toDate
    ) {
        return ResponseEntity.ok(Response.builder()
                .success(true)
                .message("Get user count successfully")
                .data(userService.countUsers(roles, fromDate, toDate))
                .timestamp(LocalDateTime.now())
                .build());
    }

}
