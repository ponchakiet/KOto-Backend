package poncha.kiet.paymentservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import poncha.kiet.paymentservice.entity.PurposeType;
import poncha.kiet.paymentservice.entity.TransactionType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestDTO {
    private String userId;
    private String purposeId;
    private Double amount;
    private PurposeType purposeType;
    private TransactionType transactionType;
}