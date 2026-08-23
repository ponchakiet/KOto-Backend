package poncha.kiet.paymentservice.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InitPaymentResponse {
    private String vnpUrl;
}
