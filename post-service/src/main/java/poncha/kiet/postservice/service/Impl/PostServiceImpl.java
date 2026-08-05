package poncha.kiet.postservice.service.Impl;

import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poncha.kiet.postservice.dto.request.PostRequestDTO;
import poncha.kiet.postservice.dto.response.PostResponseDTO;
import poncha.kiet.postservice.dto.response.VehicleDetailResponseDTO;
import poncha.kiet.postservice.entity.Post;
import poncha.kiet.postservice.entity.PostStatus;
import poncha.kiet.postservice.exception.ResourceNotFoundException;
import poncha.kiet.postservice.exception.UnauthorizedException;
import poncha.kiet.postservice.grpc.VehicleRequest;
import poncha.kiet.postservice.grpc.VehicleResponse;
import poncha.kiet.postservice.grpc.VehicleToPostServiceGrpc;
import poncha.kiet.postservice.mapper.PostMapper;
import poncha.kiet.postservice.repository.PostRepository;
import poncha.kiet.postservice.service.PostService;

import java.util.Map;

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
        post.setStatus(PostStatus.CREATED);

        postRepository.save(post);
    }

    @Override
    public void updatePost(String postId, String userId, Map<String, String> updateRequest) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        if (!post.getUserId().equals(userId))
            throw new UnauthorizedException("Don't have permission for this resource");

        if (updateRequest.containsKey("title")) {
            post.setTitle(updateRequest.get("title"));
        }

        if (updateRequest.containsKey("description")) {
            post.setDescription(updateRequest.get("description"));
        }

        postRepository.save(post);
    }

    @Override
    public void inActivePost(String userId, String postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        if (!post.getUserId().equals(userId))
            throw new UnauthorizedException("Don't have permission for this resource");

        post.setStatus(PostStatus.DELETE);
        postRepository.save(post);

    }

    @Override
    public void acceptPost(String userId, String postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        if (!post.getUserId().equals(userId))
            throw new UnauthorizedException("Don't have permission for this resource");

        post.setStatus(PostStatus.SHOW);
        postRepository.save(post);
    }

    @Override
    public PostResponseDTO findPostById(String postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Not found post with id: " + postId));

        VehicleResponse vehicleResponse = vehicleServiceBlockingStub.getVehicleDetail(VehicleRequest.newBuilder()
                .setId(post.getId())
                .build());

        PostResponseDTO postResponseDTO = postMapper.toDto(post);
        postResponseDTO.setVehicleDetail(
                VehicleDetailResponseDTO.builder()
                        .id(vehicleResponse.getId())
                        .manufactureYear(vehicleResponse.getManufactureYear())
                        .price(vehicleResponse.getPrice())
                        .name(vehicleResponse.getName())
                        .address(vehicleResponse.getAddress())
                        .ward(vehicleResponse.getWard())
                        .province(vehicleResponse.getProvince())
                        .properties(vehicleResponse.getPropertiesMap())
                        .contents(
                                vehicleResponse.getContentListList().stream()
                                        .map(c -> VehicleDetailResponseDTO.Content.builder()
                                                .url(c.getUrl())
                                                .id(c.getId())
                                                .name(c.getName())
                                                .duration(c.getDuration())
                                                .type(c.getType())
                                                .build()).toList()
                        )
                        .build());
        return postResponseDTO;
    }
}
