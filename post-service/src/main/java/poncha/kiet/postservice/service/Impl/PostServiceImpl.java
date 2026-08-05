package poncha.kiet.postservice.service.Impl;

import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poncha.kiet.postservice.dto.request.PostRequestDTO;
import poncha.kiet.postservice.entity.Post;
import poncha.kiet.postservice.grpc.VehicleRequest;
import poncha.kiet.postservice.grpc.VehicleToPostServiceGrpc;
import poncha.kiet.postservice.mapper.PostMapper;
import poncha.kiet.postservice.repository.PostRepository;
import poncha.kiet.postservice.service.PostService;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
    @GrpcClient("vehicleService")
    private VehicleToPostServiceGrpc.VehicleToPostServiceBlockingStub vehicleServiceBlockingStub;
    @GrpcClient("postService")
    private final PostMapper postMapper;
    private final PostRepository postRepository;

    @Override
    @Transactional
    public void createPost(PostRequestDTO request) {
        vehicleServiceBlockingStub.checkExisted(VehicleRequest.newBuilder()
                .setId(request.getVehicleId())
                .setUserId(request.getUserId())
                .build());

        Post post = postMapper.toEntity(request);

        postRepository.save(post);
    }
}
