package poncha.kiet.paymentservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poncha.kiet.paymentservice.dto.response.IpnResponse;
import poncha.kiet.paymentservice.dto.response.Response;
import poncha.kiet.paymentservice.service.PaymentService;
import poncha.kiet.paymentservice.utils.VnIpnResponseUtils;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {
    private final PaymentService paymentService;

    @GetMapping("/vn-pay-callback")
    public ResponseEntity<Void> payCallbackHandler(@RequestParam Map<String, String> params) {
        paymentService.process(params);

        String feUrl = "http://localhost:5173/my-ads/WAITING_ACCEPT?kw=";

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(feUrl))
                .build();
    }

    @GetMapping("/payments/sum")
    public ResponseEntity<Response<?>> sum(
            @RequestHeader("X-Roles") List<String> roles,
            @RequestParam LocalDate fromDate,
            @RequestParam LocalDate toDate) throws Exception {
        return ResponseEntity.ok(Response.builder()
                .success(true)
                .message("Sum successfully")
                .data(paymentService.sum(roles, fromDate, toDate))
                .timestamp(LocalDateTime.now())
                .build());
    }
}
