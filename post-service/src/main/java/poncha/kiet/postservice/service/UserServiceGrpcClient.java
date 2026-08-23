package poncha.kiet.postservice.service;

import poncha.kiet.postservice.grpc.UserInfoRequest;
import poncha.kiet.postservice.grpc.UserPostInfoResponse;

public interface UserServiceGrpcClient {
    UserPostInfoResponse getUserInfo(UserInfoRequest request);
}
