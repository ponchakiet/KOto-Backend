package poncha.kiet.userservice.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import poncha.kiet.userservice.dto.request.UserUpdateDTO;
import poncha.kiet.userservice.dto.response.FollowerResponse;
import poncha.kiet.userservice.dto.response.UserResponseDTO;
import poncha.kiet.userservice.entity.User;
import poncha.kiet.userservice.exception.DuplicateResourceException;
import poncha.kiet.userservice.exception.ResourceNotFoundException;
import poncha.kiet.userservice.mapper.UserMapper;
import poncha.kiet.userservice.repository.UserRepository;
import poncha.kiet.userservice.service.UserService;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public Page<UserResponseDTO> findAll(Pageable pageable) {
        Page<User> userPage = userRepository.findAll(pageable);
        return userPage.map(userMapper::toDto);
    }

    @Override
    public UserResponseDTO findById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found user with id: " + id));

        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public UserResponseDTO updateUser(String id, UserUpdateDTO request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found user with id: " + id));

        if (request.getUsername() != null && userRepository.findByUsername(request.getUsername())
                && !user.getUsername().equals(request.getUsername())) {
            throw new DuplicateResourceException("Username is already taken");
        }

        if (request.getEmail() != null && userRepository.findByEmail(request.getEmail())
                && !user.getEmail().equals(request.getEmail())) {
            throw new DuplicateResourceException("Email is already in use");
        }

        userMapper.updateUserFromRequest(request, user);

        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public void updateLastAccess(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found user with id: " + id));

        user.setLastAccess(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void updateAvatar(String id, MultipartFile file) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found user with id: " + id));

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void inActiveUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found user with id: " + id));

        user.setActive(false);
        userRepository.save(user);
    }

    @Override
    public Page<FollowerResponse> getFollowers(String id, Pageable pageable) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found user with id: " + id));

        Page<User> pageFollower = userRepository.findFollowerByUserId(id, pageable);

        return pageFollower.map(userMapper::toFollowerResponse);
    }

    @Override
    public Page<FollowerResponse> getFollowing(String id, Pageable pageable) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found user with id: " + id));

        Page<User> pageFollower = userRepository.findFollowingByUserId(id, pageable);

        return pageFollower.map(userMapper::toFollowerResponse);
    }

    @Override
    @Transactional
    public FollowerResponse followUser(String userId, String followerId) {
        //follower id is people that click follow
        //user id is followed by follower
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Not found user with id: " + userId));

        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new ResourceNotFoundException("Not found user with id: " + followerId));

        if(userId.equals(followerId)) {
            throw new IllegalArgumentException("You can not follow yourself");
        }

        userRepository.followUser(followerId, userId);

        return userMapper.toFollowerResponse(user);
    }

    @Override
    @Transactional
    public void unfollowUser(String userId, String followerId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Not found user with id: " + userId));

        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new ResourceNotFoundException("Not found user with id: " + followerId));

        userRepository.unfollowUser(followerId, userId);
    }


}
