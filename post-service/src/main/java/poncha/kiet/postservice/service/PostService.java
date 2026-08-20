package poncha.kiet.postservice.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import poncha.kiet.postservice.dto.request.PostRequestDTO;
import poncha.kiet.postservice.dto.response.*;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Map;

public interface PostService {
    void createPost(PostRequestDTO request, String userId);

    Map<String, String> updatePost(String postId, String userId, PostRequestDTO updateRequest) throws AccessDeniedException;

    void inActivePost(String userId, String postId);

    void acceptPost(String userId, String postId);

    void likePost(String userId, String postId);

    void unlikePost(String userId, String postId);

    PostDetailResponseDTO findPostById(String postId);

    Page<PostHistoryResponseDTO> findHistoryPost(String vehicleId, String userId, Pageable pageable);

    Page<PostResponseDTO> findAll(Pageable pageable, String type);

    List<PostResponseDTO> findAll();

    Page<PostOverviewResponseDTO> findAllByStatus(String status, String kw, String userId, Pageable pageable);

    Map<String, String> createComment(String postId, String userId, Map<String, String> content);

    void deleteComment(String postId, Map<String, String> content);

    Page<CommentResponseDTO> findCommentsByPostId(String postId, Pageable pageable);

    List<StatusCountResponseDTO> countStatusPost(String userId);
}
