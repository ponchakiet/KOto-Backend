package poncha.kiet.postservice.service.Impl;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import poncha.kiet.postservice.entity.PostStatus;
import poncha.kiet.postservice.grpc.PostCheckAttachRequest;
import poncha.kiet.postservice.grpc.PostCheckAttachResponse;
import poncha.kiet.postservice.grpc.PostToVehicleServiceGrpc;
import poncha.kiet.postservice.repository.PostRepository;

@GrpcService
@RequiredArgsConstructor
public class PostServiceGrpcImpl extends PostToVehicleServiceGrpc.PostToVehicleServiceImplBase {
    private final PostRepository postRepository;

    @Override
    public void checkAttachedPost(PostCheckAttachRequest request, StreamObserver<PostCheckAttachResponse> responseObserver) {
        boolean isAttached = postRepository.existsByVehicleIdAndStatus(request.getId(), PostStatus.SHOW);
        PostCheckAttachResponse response = PostCheckAttachResponse.newBuilder()
                .setIsAttached(isAttached)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
