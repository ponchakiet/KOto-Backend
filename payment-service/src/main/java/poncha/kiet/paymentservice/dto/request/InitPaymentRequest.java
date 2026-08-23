package poncha.kiet.paymentservice.dto.request;

import lombok.Data;

@Data
public class InitPaymentRequest {
    private String txnRef; //orderId
    private Integer amount; //totalOrderIdAmount
    private String ipAddress; //ipaddress
}
