package poncha.kiet.vehicleservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import poncha.kiet.vehicleservice.dto.request.DealerRequestDTO;
import poncha.kiet.vehicleservice.dto.response.Response;
import poncha.kiet.vehicleservice.service.DealerService;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/dealers")
@RequiredArgsConstructor
public class DealerController {
    private final DealerService dealerService;
    @PostMapping
    public ResponseEntity<Response<Void>> createDealer(@RequestBody DealerRequestDTO request) {
        dealerService.createDealer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Response.<Void>builder()
                .success(true)
                .message("Create dealer successfully")
                .timestamp(LocalDateTime.now())
                .build());
    }
}
