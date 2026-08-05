package poncha.kiet.postservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import poncha.kiet.postservice.entity.Post;

@Repository
public interface PostRepository extends JpaRepository<Post,String> {
}
