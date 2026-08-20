package poncha.kiet.userservice.service.impl;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import poncha.kiet.userservice.entity.User;
import poncha.kiet.userservice.grpc.UserRequest;
import poncha.kiet.userservice.grpc.UserResponse;
import poncha.kiet.userservice.grpc.UserToVehicleServiceGrpc;
import poncha.kiet.userservice.repository.UserRepository;

import java.util.Optional;

@GrpcService
@RequiredArgsConstructor
public class UserToVehicleGrpcImpl extends UserToVehicleServiceGrpc.UserToVehicleServiceImplBase {
    private final UserRepository userRepository;

    @Override
    public void getUserById(UserRequest userRequest, StreamObserver<UserResponse> responseObserver) {
        Optional<User> userOptional = userRepository.findById(userRequest.getId());
        if (!userOptional.isPresent()) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("User not found with ID: " + userRequest.getId())
                            .asRuntimeException());
            return;
        }
        User user =  userOptional.get();
        responseObserver.onNext(UserResponse.newBuilder()
                .setId(user.getId())
                .build());
        responseObserver.onCompleted();
    }
}
