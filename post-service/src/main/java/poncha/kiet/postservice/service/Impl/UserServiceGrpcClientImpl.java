package poncha.kiet.postservice.service.Impl;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import poncha.kiet.postservice.grpc.UserInfoRequest;
import poncha.kiet.postservice.grpc.UserPostInfoResponse;
import poncha.kiet.postservice.grpc.UserToPostServiceGrpc;
import poncha.kiet.postservice.service.UserServiceGrpcClient;

@Service
@Slf4j
public class UserServiceGrpcClientImpl implements UserServiceGrpcClient {
    @GrpcClient("userServiceGrpc")
    private UserToPostServiceGrpc.UserToPostServiceBlockingStub userServiceBlockingStub;

    @Override
    @CircuitBreaker(name = "userServiceCB", fallbackMethod = "fallbackUser")
    @Retry(name = "userServiceRetry")
    public UserPostInfoResponse getUserInfo(UserInfoRequest request) {
        log.info("Calling userToPostService for userId={}", request.getId());
        return userServiceBlockingStub.getUserInfo(request);
    }

    public UserPostInfoResponse fallbackUser(UserInfoRequest request, Throwable t) {
        return UserPostInfoResponse.newBuilder()
                .setId(request.getId())
                .setFirstName("Anonymous")
                .setLastName("Anonymous")
                .build();
    }
}
