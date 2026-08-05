package poncha.kiet.postservice.service;

import poncha.kiet.postservice.dto.request.PostRequestDTO;

public interface PostService {
    void createPost(PostRequestDTO request);
}
