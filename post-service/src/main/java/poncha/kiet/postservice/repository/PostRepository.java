package poncha.kiet.postservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import poncha.kiet.postservice.entity.Post;
import poncha.kiet.postservice.entity.PostType;
import poncha.kiet.postservice.projection.PostOverview;
import poncha.kiet.postservice.projection.StatusCount;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post,String> {
    Page<Post> findAllByType(Pageable pageable, PostType type);

    Page<PostOverview> findAllByStatus(Pageable pageable, String status);

    @Query(value = """
            SELECT s.status AS status, COUNT(p.id) AS count
            FROM (
                SELECT 'SHOW' AS status
                UNION ALL SELECT 'EXPIRED'
                UNION ALL SELECT 'REJECT'
                UNION ALL SELECT 'PAYMENT'
                UNION ALL SELECT 'HIDE'
                UNION ALL SELECT 'WAITING_PAYMENT'
                UNION ALL SELECT 'WAITING_ACCEPT'
            ) AS s
            LEFT JOIN posts p ON p.status = s.status
            AND p.user_id =:userId
            GROUP BY s.status
            """, nativeQuery = true)
    List<StatusCount> countStatusPost(@Param("userId") String userId);
}
