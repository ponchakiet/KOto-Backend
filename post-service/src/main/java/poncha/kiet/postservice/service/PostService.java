package poncha.kiet.postservice.service;

import poncha.kiet.postservice.dto.request.PostRequestDTO;
import poncha.kiet.postservice.dto.response.PostResponseDTO;

import java.util.Map;

public interface PostService {
    void createPost(PostRequestDTO request);

    void updatePost(String postId, String userId, Map<String, String> updateRequest);

    void inActivePost(String userId, String postId);

    void acceptPost(String userId, String postId);

    PostResponseDTO findPostById(String postId);
}
