package poncha.kiet.postservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "post_likes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostLike {
    @Id
    private String id;

    @ManyToOne
    @MapsId
    @JoinColumn(name = "id")
    private Post post;

    private String userId;
}
