package poncha.kiet.vehicleservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poncha.kiet.vehicleservice.dto.response.Response;
import poncha.kiet.vehicleservice.dto.request.VehicleRequestDTO;
import poncha.kiet.vehicleservice.dto.response.VehicleResponseDTO;
import poncha.kiet.vehicleservice.service.VehicleService;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/vehicles")
@RequiredArgsConstructor
public class VehicleController {
    private final VehicleService vehicleService;

    @GetMapping("/{vehicleId}")
    public ResponseEntity<Response<VehicleResponseDTO>> getVehicleById(@PathVariable("vehicleId") String vehicleId) {
        return ResponseEntity.ok(Response.<VehicleResponseDTO>builder()
                .success(true)
                .message("Get vehicle successfully")
                .data(vehicleService.findVehicleById(vehicleId))
                .timestamp(LocalDateTime.now())
                .build());
    }

    @PostMapping
    public ResponseEntity<Response<Void>> createVehicle(@RequestBody VehicleRequestDTO vehicleRequestDTO) {
        vehicleService.createVehicle(vehicleRequestDTO);
        return ResponseEntity.ok(Response.<Void>builder()
                .success(true)
                .message("Create vehicle successfully")
                .timestamp(LocalDateTime.now())
                .build());
    }
}
