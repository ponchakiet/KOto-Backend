package poncha.kiet.userservice.service.impl;

import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import poncha.kiet.userservice.dto.request.UserUpdateDTO;
import poncha.kiet.userservice.dto.response.FollowerResponse;
import poncha.kiet.userservice.dto.response.UserProfileResponseDTO;
import poncha.kiet.userservice.dto.response.UserResponseDTO;
import poncha.kiet.userservice.entity.User;
import poncha.kiet.userservice.exception.DuplicateResourceException;
import poncha.kiet.userservice.exception.ResourceNotFoundException;
import poncha.kiet.userservice.grpc.FileChunk;
import poncha.kiet.userservice.grpc.UploadResponse;
import poncha.kiet.userservice.grpc.UploadServiceGrpc;
import poncha.kiet.userservice.mapper.UserMapper;
import poncha.kiet.userservice.projection.UserProfile;
import poncha.kiet.userservice.repository.UserRepository;
import poncha.kiet.userservice.service.UserService;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Map;
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
    public UserProfileResponseDTO findProfileUser(String id) {
        UserProfile user = userRepository.findProfileUser(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found user"));

        return userMapper.toResponseDto(user);
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
    public Map<String, String> updateAvatar(String id, MultipartFile file) {
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
            user.setAvtUrl(responses[0].getUrl());
            userRepository.save(user);

            return Map.of("avtUrl", user.getAvtUrl());
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
    public FollowerResponse followUser(String userId, String followerId) {
        //follower id is people that click follow
        //user id is followed by follower
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Not found user with id: " + userId));

        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new ResourceNotFoundException("Not found user with id: " + followerId));

        if (userId.equals(followerId)) {
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
