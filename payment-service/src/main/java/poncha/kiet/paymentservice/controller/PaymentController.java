package poncha.kiet.paymentservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import poncha.kiet.paymentservice.dto.response.IpnResponse;
import poncha.kiet.paymentservice.service.PaymentService;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @GetMapping("/vn-pay-callback")
    public IpnResponse payCallbackHandler(@RequestParam Map<String, String> params) {
        return paymentService.process(params);
    }
}
