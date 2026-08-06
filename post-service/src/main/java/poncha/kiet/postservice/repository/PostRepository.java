package poncha.kiet.postservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import poncha.kiet.postservice.entity.Post;
import poncha.kiet.postservice.entity.PostType;

@Repository
public interface PostRepository extends JpaRepository<Post,String> {
    Page<Post> findAllByType(Pageable pageable, PostType type);
}
