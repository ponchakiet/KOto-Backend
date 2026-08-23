package poncha.kiet.postservice.service.Impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poncha.kiet.postservice.dto.request.PostRequestDTO;
import poncha.kiet.postservice.dto.response.*;
import poncha.kiet.postservice.entity.*;
import poncha.kiet.postservice.exception.ResourceNotFoundException;
import poncha.kiet.postservice.exception.UnauthorizedException;
import poncha.kiet.postservice.grpc.*;
import poncha.kiet.postservice.mapper.PostMapper;
import poncha.kiet.postservice.mapper.UserMapper;
import poncha.kiet.postservice.mapper.VehicleMapper;
import poncha.kiet.postservice.projection.PostHistory;
import poncha.kiet.postservice.projection.PostOverview;
import poncha.kiet.postservice.projection.StatusCount;
import poncha.kiet.postservice.repository.CommentRepository;
import poncha.kiet.postservice.repository.PostRepository;
import poncha.kiet.postservice.service.PostService;
import poncha.kiet.postservice.utils.AppConstant;
import poncha.kiet.postservice.utils.Helper;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
    private final UserMapper userMapper;
    @GrpcClient("vehicleServiceGrpc")
    private VehicleToPostServiceGrpc.VehicleToPostServiceBlockingStub vehicleServiceBlockingStub;
    @GrpcClient("userServiceGrpc")
    private UserToPostServiceGrpc.UserToPostServiceBlockingStub userServiceBlockingStub;
    @GrpcClient("paymentServiceGrpc")
    private PaymentServiceGrpc.PaymentServiceBlockingStub paymentServiceBlockingStub;
    private final PostMapper postMapper;
    private final PostRepository postRepository;
    private final VehicleMapper vehicleMapper;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public Map<String, String> createPost(PostRequestDTO request, String userId, HttpServletRequest httpServletRequest) {
        vehicleServiceBlockingStub.checkExisted(VehicleRequest.newBuilder()
                .setId(request.getVehicleId())
                .setUserId(userId)
                .build());

        Post post = postMapper.toEntity(request);
        post.setUserId(userId);
        post.setStatus(PostStatus.WAITING_PAYMENT);
        postRepository.save(post);
        postRepository.flush();

        PaymentUrlResponse response = paymentServiceBlockingStub.getPaymentUrl(
                PaymentUrlRequest.newBuilder()
                        .setAmount(AppConstant.PRICE_CREATED)
                        .setPurposeType(PurposeType.POST.name())
                        .setTransactionType(TransactionType.CREATED.name())
                        .setIpAddress(Helper.getIpAddress(httpServletRequest))
                        .setTxnRef(post.getId())
                        .build()
        );

        return Map.of("paymentUrl", response.getVnpUrl());
    }

    @Override
    public Map<String, String> updatePost(String postId, String userId, PostRequestDTO updateRequest) throws AccessDeniedException {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        if (!post.getUserId().equals(userId))
            throw new AccessDeniedException("Don't have permission for this resource");

        postMapper.update(updateRequest, post);
        postRepository.save(post);
        return Map.of("id", post.getId());
    }

    @Override
    public void inActivePost(String userId, List<String> roles, String postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        if (!post.getUserId().equals(userId) && !roles.contains(UserRole.ROLE_WEBMASTER.name()))
            throw new UnauthorizedException("Don't have permission for this resource");

        post.setStatus(PostStatus.DELETE);
        postRepository.save(post);

    }

    @Override
    public Map<String, String> hidePost(String userId, List<String> roles, String postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Not found post"));

        if (post.getStatus() != PostStatus.SHOW)
            throw new RuntimeException("Invalid status");

        if (!post.getUserId().equals(userId) && !roles.contains(UserRole.ROLE_WEBMASTER.name()))
            throw new UnauthorizedException("Don't have permission for this resource");

        post.setStatus(PostStatus.HIDE);
        postRepository.save(post);

        return Map.of("id", postId);
    }

    @Override
    public void rejectPost(List<String> roles, String postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Not found post"));

        if (roles == null || !roles.contains(UserRole.ROLE_WEBMASTER.name()))
            throw new UnauthorizedException("Don't have permission for this resource");

        post.setStatus(PostStatus.REJECT);
        postRepository.save(post);
    }

    @Override
    public void acceptPost(List<String> roles, String postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        if (roles == null || !roles.contains(UserRole.ROLE_WEBMASTER.name()))
            throw new UnauthorizedException("Don't have permission for this resource");

        post.setStatus(PostStatus.SHOW);
        postRepository.save(post);
    }

    @Override
    public void updateStatusPost(String postId, PostStatus status) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Not found post"));

        post.setStatus(status);
        postRepository.save(post);
    }

    @Override
    @Transactional
    public void deletePost(String postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Not found post"));

        postRepository.delete(post);
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
    public PostDetailResponseDTO findPostById(String postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Not found post with id: " + postId));

        return postMapper.toDetailResponse(post);
    }

    @Override
    public Page<PostHistoryResponseDTO> findHistoryPost(String vehicleId, String userId, Pageable pageable) {
        Page<PostHistory> postHistories = postRepository.findAllByVehicleIdAndUserId(vehicleId, userId, pageable);

        return postHistories.map(postMapper::toHistory);
    }

    @Override
    public Page<PostResponseDTO> findAll(Pageable pageable, String type) {
        Page<Post> postPage = postRepository.findAllByType(pageable, PostType.valueOf(type), PostStatus.SHOW);

        Page<PostResponseDTO> posts = postPage
                .map(p -> {
                    VehicleResponse vehicleResponse = vehicleServiceBlockingStub.getVehicleDetail(VehicleRequest.newBuilder()
                            .setId(p.getVehicleId())
                            .build());

                    UserPostInfoResponse userPostInfoResponse = userServiceBlockingStub.getUserInfo(UserInfoRequest.newBuilder()
                            .setId(p.getUserId())
                            .build());

                    PostResponseDTO postResponseDTO = postMapper.toDto(p);
                    postResponseDTO.setVehicleDetail(vehicleMapper.toDto(vehicleResponse));
                    postResponseDTO.setUserInfo(userMapper.toResponseDTO(userPostInfoResponse));
                    return postResponseDTO;
                });

        return posts;
    }

    @Override
    public List<PostResponseDTO> findAll() {
        List<PostResponseDTO> postResponseDTOS = postRepository.findAll().stream()
                .map(p -> {
                    VehicleResponse vehicleResponse = vehicleServiceBlockingStub.getVehicleDetail(
                            VehicleRequest.newBuilder().setId(p.getVehicleId()).build()
                    );
                    PostResponseDTO postResponseDTO = postMapper.toDto(p);
                    postResponseDTO.setVehicleDetail(vehicleMapper.toDto(vehicleResponse));
                    return postResponseDTO;
                }).toList();

        return postResponseDTOS;
    }

    @Override
    public Page<PostOverviewResponseDTO> findAllByStatus(String status, String kw, String userId, Pageable pageable) {
        Page<PostOverview> postOverviews = postRepository.findAllByStatus(pageable, userId, kw, PostStatus.valueOf(status));

        Page<PostOverviewResponseDTO> data = postOverviews
                .map(p -> {
                    PostOverviewResponseDTO postOverviewResponseDTO = postMapper.toOverview(p);
                    Content content = vehicleServiceBlockingStub.getPoster(
                            VehicleRequest.newBuilder()
                                    .setUserId(p.getUserId())
                                    .setId(p.getVehicleId())
                                    .build()
                    );
                    postOverviewResponseDTO.setPosterUrl(content.getUrl());
                    return postOverviewResponseDTO;
                });

        return data;
    }

    @Override
    public Page<PostWebmasterOverviewResponseDTO> findAllByStatus(String status, String kw, Pageable pageable) {
        PostStatus postStatus = status != null ? PostStatus.valueOf(status) : null;
        Page<PostOverview> postOverviews = postRepository.findAllByStatus(pageable, null, kw, postStatus);
        Page<PostWebmasterOverviewResponseDTO> data = postOverviews
                .map(p -> {
                    PostWebmasterOverviewResponseDTO dto = postMapper.toWebmasterResponse(p);

                    UserPostInfoResponse userPostInfoResponse = userServiceBlockingStub.getUserInfo(
                            UserInfoRequest.newBuilder()
                                    .setId(p.getUserId())
                                    .build());

                    dto.setUserInfo(userMapper.toResponseDTO(userPostInfoResponse));

                    return dto;
                });

        return data;
    }

    @Override
    @Transactional
    public Map<String, String> createComment(String postId, String userId, Map<String, String> content) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Not found post"));

        Comment comment = Comment.builder()
                .userId(userId)
                .createdAt(LocalDateTime.now())
                .content(content.get("content"))
                .build();

        post.addComment(comment);
        postRepository.save(post);

        comment = commentRepository.save(comment);

        return Map.of("id", comment.getId(),
                "content", comment.getContent(),
                "createdAt", comment.getCreatedAt().toString());

    }

    @Override
    public void deleteComment(String postId, Map<String, String> content) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Not found post"));

        Comment comment = post.getComments().stream()
                .filter(c -> c.getId().equals(content.get("commentId")))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Not found comment"));

        if (!comment.getUserId().equals(content.get("userId")))
            throw new UnauthorizedException("Don't have permission for this resource");

        post.removeComment(comment);
        postRepository.save(post);
    }

    @Override
    public Page<CommentResponseDTO> findCommentsByPostId(String postId, Pageable pageable) {
        Page<Comment> comments = commentRepository.findAllByPostId(postId, pageable);

        Page<CommentResponseDTO> response = comments
                .map(c -> {
                    CommentResponseDTO commentResponseDTO = CommentResponseDTO.builder()
                            .id(c.getId())
                            .content(c.getContent())
                            .createdAt(c.getCreatedAt())
                            .build();

                    UserPostInfoResponse userPostInfoResponse = userServiceBlockingStub
                            .getUserInfo(UserInfoRequest.newBuilder()
                                    .setId(c.getUserId())
                                    .build());

                    commentResponseDTO.setUserInfo(CommentResponseDTO.UserInfo.builder()
                            .firstName(userPostInfoResponse.getFirstName())
                            .lastName(userPostInfoResponse.getLastName())
                            .avtUrl(userPostInfoResponse.getAvtUrl())
                            .id(userPostInfoResponse.getId())
                            .build());

                    return commentResponseDTO;
                });

        return response;
    }

    @Override
    public List<StatusCountResponseDTO> countStatusPost(String userId) {
        List<StatusCount> counts = postRepository.countStatusPost(userId);

        return counts.stream()
                .map(c -> StatusCountResponseDTO.builder()
                        .count(c.getCount())
                        .name(c.getStatus())
                        .build())
                .toList();
    }
}
