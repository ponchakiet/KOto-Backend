package poncha.kiet.userservice.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import poncha.kiet.userservice.dto.request.UserUpdateDTO;
import poncha.kiet.userservice.dto.response.FollowerResponse;
import poncha.kiet.userservice.dto.response.UserResponseDTO;

import java.util.Map;

public interface UserService {
    Page<UserResponseDTO> findAll(Pageable pageable);
    UserResponseDTO findById(String id);
    UserResponseDTO updateUser(String id, UserUpdateDTO user);
    void updateLastAccess(String id);
    Map<String, String> updateAvatar(String id, MultipartFile file);
    void inActiveUser(String id);
    Page<FollowerResponse> getFollowers(String id, Pageable pageable);
    Page<FollowerResponse> getFollowing(String id, Pageable pageable);
    FollowerResponse followUser(String userId, String followerId);
    void unfollowUser(String userId, String followerId);
}
