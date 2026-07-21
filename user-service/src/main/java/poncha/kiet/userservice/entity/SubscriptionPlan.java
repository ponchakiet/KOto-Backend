package poncha.kiet.userservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "subscription_plans")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SubscriptionPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "monthly_price")
    private double monthlyPrice;
    @Column(name = "max_post")
    private int maxPost;
    @Column(name = "show_post")
    private int showPost;
}
