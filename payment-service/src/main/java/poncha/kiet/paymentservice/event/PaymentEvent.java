package poncha.kiet.paymentservice.event;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import poncha.kiet.paymentservice.dto.request.PaymentRequestDTO;
import poncha.kiet.paymentservice.entity.PaymentStatus;

import java.util.Date;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentEvent implements Event{
    private UUID eventId = UUID.randomUUID();
    private Date eventDate = new Date();
    @JsonSerialize
    private PaymentRequestDTO paymentRequestDto;
    @JsonSerialize
    private PaymentStatus paymentStatus;

    @Override
    public UUID getEventId() {
        return eventId;
    }

    @Override
    public Date getDate() {
        return eventDate;
    }
}
