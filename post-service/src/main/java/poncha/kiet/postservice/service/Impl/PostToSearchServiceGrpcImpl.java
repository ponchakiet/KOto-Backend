package poncha.kiet.postservice.service.Impl;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.server.service.GrpcService;
import poncha.kiet.postservice.grpc.*;
import poncha.kiet.postservice.mapper.PostMapper;
import poncha.kiet.postservice.repository.PostRepository;

import java.util.List;

@GrpcService
@RequiredArgsConstructor
public class PostToSearchServiceGrpcImpl extends PostToSearchServiceGrpc.PostToSearchServiceImplBase {
    private final PostRepository postRepository;
    private final PostMapper postMapper;
    @GrpcClient("vehicleServiceGrpc")
    private VehicleToPostServiceGrpc.VehicleToPostServiceBlockingStub vehicleServiceGrpc;

    @Override
    public void getAllPost(Empty request, StreamObserver<PostDetailResponseList> responseObserver) {
        List<PostDetailResponse> postDetailResponses = postRepository.findAll().stream()
                .map(p -> {
                    VehicleResponse vehicleResponse = vehicleServiceGrpc.getVehicleDetail(VehicleRequest.newBuilder()
                            .setId(p.getVehicleId())
                            .build());

                    PostDetailResponse postDetailResponse = postMapper.toDetail(p).toBuilder()
                            .setVehicleDetail(vehicleResponse).build();

                    return postDetailResponse;
                }).toList();

        PostDetailResponseList responseList = PostDetailResponseList.newBuilder()
                .addAllResponse(postDetailResponses).build();

        responseObserver.onNext(responseList);
        responseObserver.onCompleted();
    }
}
