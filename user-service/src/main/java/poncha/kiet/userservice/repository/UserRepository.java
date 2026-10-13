package poncha.kiet.userservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import poncha.kiet.userservice.entity.User;
import poncha.kiet.userservice.projection.UserProfile;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    @Query("FROM User u where u.id=:userId")
    Optional<UserProfile> findProfileUser(@Param("userId") String userId);

    <T> Optional<T> findById(String id, Class<T> type);

    @Query(value = """
            SELECT COUNT(*) FROM follower as f
            WHERE f.user_id =:userId
            """, nativeQuery = true)
    Long countFollowers(@Param("userId") String userId);

    @Query(value = """
            SELECT CASE WHEN COUNT(*) > 0 THEN 1 ELSE 0 END
            FROM follower
            WHERE follower_id =:userId
            AND user_id =:followerId
            """, nativeQuery = true)
    Long checkFollow(@Param("userId") String userId, @Param("followerId") String followerId);

    @Query(value = """
            SELECT COUNT(*) FROM  follower as f
            WHERE f.follower_id =:userId
            """, nativeQuery = true)
    Long countFollowing(@Param("userId") String userId);
    
    boolean existsByEmail(String email);

    @Query(value = """
            SELECT u.* FROM users u
            JOIN follower f
                ON u.user_id = f.follower_id
            WHERE f.user_id = :userId
            """, nativeQuery = true)
    Page<User> findFollowerByUserId(@Param("userId") String userId, Pageable pageable);

    @Query(value = """
            SELECT u.* FROM users u
            JOIN follower f
                ON u.user_id = f.user_id
            WHERE f.follower_id = :userId
            """, nativeQuery = true)
    Page<User> findFollowingByUserId(@Param("userId") String userId, Pageable pageable);

    @Modifying
    @Query(value = """
            INSERT INTO follower (user_id, follower_id)
            VALUES (:userId, :followerId)
            """, nativeQuery = true)
    void followUser(@Param("userId") String userId, @Param("followerId") String followerId);

    @Modifying
    @Query(value = """
            DELETE FROM follower f
            WHERE f.user_id = :userId
            AND f.follower_id = :followerId
            """, nativeQuery = true)
    void unfollowUser(@Param("userId") String userId, @Param("followerId") String followerId);

    long countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(LocalDateTime start, LocalDateTime end);
}
