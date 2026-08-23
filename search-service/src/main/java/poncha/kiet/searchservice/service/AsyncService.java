package poncha.kiet.searchservice.service;

import org.springframework.stereotype.Service;

@Service
public interface AsyncService {
    void migrateData();

    void createPost(String postId);

    void updatePost(String postId);

    void deletePost(String postId);
}
