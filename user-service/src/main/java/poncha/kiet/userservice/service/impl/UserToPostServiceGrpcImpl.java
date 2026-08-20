package poncha.kiet.userservice.service.impl;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import poncha.kiet.userservice.grpc.UserInfoRequest;
import poncha.kiet.userservice.grpc.UserPostInfoResponse;
import poncha.kiet.userservice.grpc.UserToPostServiceGrpc;
import poncha.kiet.userservice.mapper.UserMapper;
import poncha.kiet.userservice.projection.UserPostInfo;
import poncha.kiet.userservice.repository.UserRepository;

import java.util.Optional;

@GrpcService
@RequiredArgsConstructor
public class UserToPostServiceGrpcImpl extends UserToPostServiceGrpc.UserToPostServiceImplBase {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public void getUserInfo(UserInfoRequest request, StreamObserver<UserPostInfoResponse> responseStreamObserver) {
        Optional<UserPostInfo> userPostInfo = userRepository.findById(request.getId(), UserPostInfo.class);
        if(!userPostInfo.isPresent()){
            responseStreamObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("User not found with ID: "+request.getId())
                            .asRuntimeException()
            );
            return;
        }
        UserPostInfo user = userPostInfo.get();

        UserPostInfoResponse response = userMapper.toGrpcResponse(user);
        responseStreamObserver.onNext(response);
        responseStreamObserver.onCompleted();
    }
}
