package poncha.kiet.userservice.service.impl;

import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import poncha.kiet.userservice.dto.request.UserSignUpRequestDTO;
import poncha.kiet.userservice.dto.request.UserUpdateDTO;
import poncha.kiet.userservice.dto.response.FollowerResponse;
import poncha.kiet.userservice.dto.response.UserLandingPageResponseDTO;
import poncha.kiet.userservice.dto.response.UserProfileResponseDTO;
import poncha.kiet.userservice.dto.response.UserResponseDTO;
import poncha.kiet.userservice.entity.User;
import poncha.kiet.userservice.entity.UserRole;
import poncha.kiet.userservice.exception.DuplicateResourceException;
import poncha.kiet.userservice.exception.ResourceNotFoundException;
import poncha.kiet.userservice.exception.UnauthorizedException;
import poncha.kiet.userservice.grpc.FileChunk;
import poncha.kiet.userservice.grpc.UploadResponse;
import poncha.kiet.userservice.grpc.UploadServiceGrpc;
import poncha.kiet.userservice.mapper.UserMapper;
import poncha.kiet.userservice.projection.UserLandingPage;
import poncha.kiet.userservice.projection.UserProfile;
import poncha.kiet.userservice.repository.UserRepository;
import poncha.kiet.userservice.service.UserService;
import poncha.kiet.userservice.utils.TypeMedia;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @GrpcClient("uploadServiceGrpc")
    private UploadServiceGrpc.UploadServiceStub uploadServiceStub;

    @Override
    public Page<UserResponseDTO> findAll(Pageable pageable) {
        Page<User> userPage = userRepository.findAll(pageable);
        return userPage.map(userMapper::toDto);
    }

    @Override
    @Transactional
    public UserSignUpRequestDTO createUser(UserSignUpRequestDTO request) {
        User user = User.builder()
                .id(request.getUserId())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .createdAt(LocalDateTime.now())
                .email(request.getEmail())
                .isActive(true)
                .sex(true)
                .username(request.getUsername())
                .build();

        userRepository.save(user);
        return request;
    }

    @Override
    public UserProfileResponseDTO findProfileUser(String id, HttpServletRequest httpServletRequest) {
        Optional<UserProfile> optionalUserProfile = userRepository.findProfileUser(id);

        if (!optionalUserProfile.isPresent()) {
            String keycloakId = httpServletRequest.getHeader("X-UserId");
            String username = httpServletRequest.getHeader("X-Username");
            String email = httpServletRequest.getHeader("X-Email");
            String firstName = httpServletRequest.getHeader("X-FirstName");
            String lastName = httpServletRequest.getHeader("X-LastName");
            User user = User.builder()
                    .id(keycloakId)
                    .username(username)
                    .firstName(firstName)
                    .lastName(lastName)
                    .email(email)
                    .build();
            userRepository.save(user);
            return userMapper.toResponseDto(user);
        }
        UserProfile userProfile = optionalUserProfile.get();
        return userMapper.toResponseDto(userProfile);
    }

    @Override
    public UserResponseDTO findById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found user with id: " + id));

        return userMapper.toDto(user);
    }

    @Override
    public UserLandingPageResponseDTO findUserLandingPage(String userId) {
        UserLandingPage userLandingPage = userRepository.findById(userId, UserLandingPage.class)
                .orElseThrow(() -> new ResourceNotFoundException("Not found user"));

        Long totalFollower = userRepository.countFollowers(userId);
        Long totalFollowing = userRepository.countFollowing(userId);

        UserLandingPageResponseDTO responseDTO = userMapper.toLanding(userLandingPage);

        responseDTO.setTotalFollower(totalFollower);
        responseDTO.setTotalFollowing(totalFollowing);

        return responseDTO;
    }

    @Override
    public Map<String, String> checkUserFollow(String userId, String followerId) {
        Long isFollow = userRepository.checkFollow(userId, followerId);

        return Map.of("isFollow", isFollow.toString());
    }

    @Override
    @Transactional
    public UserResponseDTO updateUser(String id, UserUpdateDTO request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found user with id: " + id));

        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())
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
    public Map<String, String> updateMedia(String id, String type, MultipartFile file) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found user with id: " + id));

        final UploadResponse[] responses = new UploadResponse[1];
        CountDownLatch finishLatch = new CountDownLatch(1);
        StreamObserver<UploadResponse> uploadResponseStreamObserver = new StreamObserver<UploadResponse>() {
            @Override
            public void onNext(UploadResponse uploadResponse) {
                responses[0] = uploadResponse;
            }

            @Override
            public void onError(Throwable throwable) {
                finishLatch.countDown();
                throw new RuntimeException(throwable);
            }

            @Override
            public void onCompleted() {
                finishLatch.countDown();
            }
        };

        StreamObserver<FileChunk> requestObserver = uploadServiceStub.upload(uploadResponseStreamObserver);

        try (InputStream inputStream = file.getInputStream()) {
            byte[] buffer = new byte[1024 * 1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                FileChunk chunk = FileChunk.newBuilder()
                        .setContent(ByteString.copyFrom(buffer, 0, bytesRead))
                        .setFileName(file.getOriginalFilename())
                        .build();
                requestObserver.onNext(chunk);
            }
            requestObserver.onCompleted();
            if (!finishLatch.await(10, TimeUnit.SECONDS)) {
                {
                    throw new RuntimeException("Upload timed out");
                }
            }
            if (responses[0] == null) {
                throw new RuntimeException("No response received from upload service");
            }
            Map<String, String> data = new HashMap<>();
            if (TypeMedia.AVT.name().equals(type)) {
                user.setAvtUrl(responses[0].getUrl());
                data.put("avtUrl", user.getAvtUrl());
            } else {
                user.setBannerUrl(responses[0].getUrl());
                data.put("bannerUrl", user.getBannerUrl());
            }
            userRepository.save(user);

            return data;
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file", e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
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
    public Map<String, String> followUser(String userId, String followerId) {
        //follower id is people that click follow
        //user id is followed by follower
        Long checkFollow = userRepository.checkFollow(userId, followerId);
        if (checkFollow > 0) {
            throw new RuntimeException("User follow is existed");
        }

        userRepository.followUser(followerId, userId);

        return Map.of("followerId", followerId);
    }

    @Override
    @Transactional
    public Map<String, String> unfollowUser(String userId, String followerId) {
        Long checkFollow = userRepository.checkFollow(userId, followerId);
        if (checkFollow < 0) {
            throw new RuntimeException("User follow is not existed");
        }

        userRepository.unfollowUser(followerId, userId);

        return Map.of("followerId", followerId);
    }

    @Override
    public Map<String, Long> countUsers(List<String> roles, LocalDate fromDate, LocalDate toDate) {
        if (!roles.contains(UserRole.ROLE_WEBMASTER.name()))
            throw new UnauthorizedException("Don't have permission for this resource");

        LocalDateTime start = fromDate.atStartOfDay();
        LocalDateTime end = toDate
                .plusDays(1)
                .atStartOfDay();

        long count = userRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(start, end);

        return Map.of("totalUsers", count);
    }
}
