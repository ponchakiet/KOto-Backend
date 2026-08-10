package poncha.kiet.postservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(name = "last_updated")
    @LastModifiedDate
    private LocalDateTime lastUpdated;

    @Column(name = "vehicle_id")
    private String vehicleId;

    @Column(name = "user_id")
    private String userId;
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private PostType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private PostStatus status;


    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostLike> userLike;

    public void addComment(Comment comment) {
        if (comments != null) {
            comments = new ArrayList<>();
        }

        comment.setPost(this);
        comments.add(comment);
    }

    public void removeComment(Comment comment) {
        if (comments != null)
            comments = new ArrayList<>();

        comments.remove(comment);
        comment.setPost(null);
    }

    public void addLikePost(PostLike postLike) {
        if (userLike == null)
            userLike = new ArrayList<>();

        postLike.setPost(this);
        userLike.add(postLike);
    }

    public void removeLikePost(String userId) {
        if (userLike == null)
            userLike = new ArrayList<>();

        PostLike postLike = userLike.stream()
                .filter(p -> p.getUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Something went wrong"));

        userLike.remove(postLike);
        postLike.setPost(null);
    }
}
