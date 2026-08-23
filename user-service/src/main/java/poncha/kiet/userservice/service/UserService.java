package poncha.kiet.userservice.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import poncha.kiet.userservice.dto.request.UserSignUpRequestDTO;
import poncha.kiet.userservice.dto.request.UserUpdateDTO;
import poncha.kiet.userservice.dto.response.FollowerResponse;
import poncha.kiet.userservice.dto.response.UserLandingPageResponseDTO;
import poncha.kiet.userservice.dto.response.UserProfileResponseDTO;
import poncha.kiet.userservice.dto.response.UserResponseDTO;

import java.util.Map;

public interface UserService {
    Page<UserResponseDTO> findAll(Pageable pageable);

    UserSignUpRequestDTO createUser(UserSignUpRequestDTO request);

    UserProfileResponseDTO findProfileUser(String id, HttpServletRequest httpServletRequest);

    UserResponseDTO findById(String id);

    UserLandingPageResponseDTO findUserLandingPage(String userId);

    Map<String, String> checkUserFollow(String userId, String followerId);

    UserResponseDTO updateUser(String id, UserUpdateDTO user);

    void updateLastAccess(String id);

    Map<String, String> updateMedia(String id, String type, MultipartFile file);

    void inActiveUser(String id);

    Page<FollowerResponse> getFollowers(String id, Pageable pageable);

    Page<FollowerResponse> getFollowing(String id, Pageable pageable);

    Map<String, String> followUser(String userId, String followerId);

    Map<String, String> unfollowUser(String userId, String followerId);
}
