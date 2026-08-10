package poncha.kiet.postservice.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import poncha.kiet.postservice.dto.request.PostRequestDTO;
import poncha.kiet.postservice.dto.response.CommentResponseDTO;
import poncha.kiet.postservice.dto.response.PostOverviewResponseDTO;
import poncha.kiet.postservice.dto.response.PostResponseDTO;
import poncha.kiet.postservice.dto.response.StatusCountResponseDTO;

import java.util.List;
import java.util.Map;

public interface PostService {
    void createPost(PostRequestDTO request);

    Map<String, String> updatePost(String postId, String userId, Map<String, String> updateRequest);

    void inActivePost(String userId, String postId);

    void acceptPost(String userId, String postId);

    void likePost(String userId, String postId);

    void unlikePost(String userId, String postId);

    PostResponseDTO findPostById(String postId);

    Page<PostResponseDTO> findAll(Pageable pageable, String type);

    Page<PostOverviewResponseDTO> findAllByStatus(String status, String userId, Pageable pageable);

    Map<String, String> createComment(String postId, Map<String, String> content);

    void deleteComment(String postId, Map<String, String> content);

    Page<CommentResponseDTO> findCommentsByPostId(String postId, Pageable pageable);

    List<StatusCountResponseDTO> countStatusPost(String userId);
}
