package poncha.kiet.postservice.service.Impl;

import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poncha.kiet.postservice.dto.request.PostRequestDTO;
import poncha.kiet.postservice.dto.response.CommentResponseDTO;
import poncha.kiet.postservice.dto.response.PostResponseDTO;
import poncha.kiet.postservice.entity.*;
import poncha.kiet.postservice.exception.ResourceNotFoundException;
import poncha.kiet.postservice.exception.UnauthorizedException;
import poncha.kiet.postservice.grpc.VehicleRequest;
import poncha.kiet.postservice.grpc.VehicleResponse;
import poncha.kiet.postservice.grpc.VehicleToPostServiceGrpc;
import poncha.kiet.postservice.mapper.CommentMapper;
import poncha.kiet.postservice.mapper.PostMapper;
import poncha.kiet.postservice.mapper.VehicleMapper;
import poncha.kiet.postservice.repository.CommentRepository;
import poncha.kiet.postservice.repository.PostRepository;
import poncha.kiet.postservice.service.PostService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
    @GrpcClient("vehicleService")
    private VehicleToPostServiceGrpc.VehicleToPostServiceBlockingStub vehicleServiceBlockingStub;
    private final PostMapper postMapper;
    private final PostRepository postRepository;
    private final VehicleMapper vehicleMapper;
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;

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
    public Map<String, String> updatePost(String postId, String userId, Map<String, String> updateRequest) {
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
        return updateRequest;
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
    @Transactional
    public void likePost(String userId, String postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Not found post with id: " + postId));

        PostLike postLike = PostLike.builder()
                .postId(postId)
                .userId(userId)
                .build();

        post.addLikePost(postLike);
        postRepository.save(post);
    }

    @Override
    @Transactional
    public void unlikePost(String userId, String postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Not found post with id: " + postId));

        post.removeLikePost(userId);
        postRepository.save(post);
    }

    @Override
    public PostResponseDTO findPostById(String postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Not found post with id: " + postId));

        VehicleResponse vehicleResponse = vehicleServiceBlockingStub.getVehicleDetail(VehicleRequest.newBuilder()
                .setId(post.getVehicleId())
                .build());

        PostResponseDTO postResponseDTO = postMapper.toDto(post);
        postResponseDTO.setVehicleDetail(vehicleMapper.toDto(vehicleResponse));
        return postResponseDTO;
    }

    @Override
    public Page<PostResponseDTO> findAll(Pageable pageable, String type) {
        Page<Post> postPage = postRepository.findAllByType(pageable, PostType.valueOf(type));

        Page<PostResponseDTO> posts = postPage
                .map(p -> {
                    VehicleResponse vehicleResponse = vehicleServiceBlockingStub.getVehicleDetail(VehicleRequest.newBuilder()
                            .setId(p.getVehicleId())
                            .build());
                    PostResponseDTO postResponseDTO = postMapper.toDto(p);
                    postResponseDTO.setVehicleDetail(vehicleMapper.toDto(vehicleResponse));
                    return postResponseDTO;
                });

        return posts;
    }

    @Override
    @Transactional
    public Map<String, String> createComment(String postId, Map<String, String> content) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Not found post"));

        Comment comment = Comment.builder()
                .userId(content.get("userId"))
                .createdAt(LocalDateTime.now())
                .content(content.get("content"))
                .build();

        post.addComment(comment);
        postRepository.save(post);

        comment = commentRepository.save(comment);

        return Map.of("id", comment.getId());

    }

    @Override
    public void deleteComment(String postId, Map<String, String> content) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Not found post"));

        Comment comment = post.getComments().stream()
                .filter(c -> c.getId().equals(content.get("commentId")))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Not found comment"));

        if(!comment.getUserId().equals(content.get("userId")))
            throw new UnauthorizedException("Don't have permission for this resource");

        post.removeComment(comment);
        postRepository.save(post);
    }

    @Override
    public Page<CommentResponseDTO> findCommentsByPostId(String postId, Pageable pageable) {
        Page<Comment> comments = commentRepository.findAllByPostId(postId, pageable);
        return comments.map(commentMapper::toDto);
    }
}
