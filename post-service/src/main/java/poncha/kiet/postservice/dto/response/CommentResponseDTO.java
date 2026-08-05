package poncha.kiet.postservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CommentResponseDTO {
    private String id;
    private String content;
    private LocalDateTime createdAt;
}
