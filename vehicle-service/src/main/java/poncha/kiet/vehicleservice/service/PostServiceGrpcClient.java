package poncha.kiet.vehicleservice.service;

import poncha.kiet.vehicleservice.grpc.PostCheckAttachRequest;
import poncha.kiet.vehicleservice.grpc.PostCheckAttachResponse;

public interface PostServiceGrpcClient {
    PostCheckAttachResponse checkAttachedPost(PostCheckAttachRequest request);
}
