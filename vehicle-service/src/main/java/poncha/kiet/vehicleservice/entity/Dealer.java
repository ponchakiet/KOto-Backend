package poncha.kiet.vehicleservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "dealer")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dealer {
    @Id
    private String id;
    private LocalDateTime foundingDate;
    private String name;
    private String description;
}
