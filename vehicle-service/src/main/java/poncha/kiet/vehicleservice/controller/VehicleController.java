package poncha.kiet.vehicleservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import poncha.kiet.vehicleservice.dto.request.UploadImageRequestDTO;
import poncha.kiet.vehicleservice.dto.response.PageResponse;
import poncha.kiet.vehicleservice.dto.response.Response;
import poncha.kiet.vehicleservice.dto.request.VehicleRequestDTO;
import poncha.kiet.vehicleservice.dto.response.VehicleResponseDTO;
import poncha.kiet.vehicleservice.dto.response.VehicleSummaryResponseDTO;
import poncha.kiet.vehicleservice.entity.VehicleType;
import poncha.kiet.vehicleservice.service.VehicleService;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/vehicles")
@RequiredArgsConstructor
public class VehicleController {
    private final VehicleService vehicleService;

    @GetMapping
    public ResponseEntity<Response<PageResponse<VehicleSummaryResponseDTO>>> getVehicles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        String userId = "user-1";
        return ResponseEntity.ok(Response.<PageResponse<VehicleSummaryResponseDTO>>builder()
                .success(true)
                .message("Get vehicles successfully")
                .data(PageResponse.build(vehicleService.findAll(userId, PageRequest.of(page, size))))
                .timestamp(LocalDateTime.now())
                .build());
    }

    @GetMapping("/draft")
    public ResponseEntity<Response<List<VehicleResponseDTO>>> getVehiclesDraft() {
        String userId = "user-1";
        String type = String.valueOf(VehicleType.DRAFT);

        return ResponseEntity.ok(Response.<List<VehicleResponseDTO>>builder()
                .success(true)
                .message("Get vehicles draft successfully")
                .data(vehicleService.findVehicleByType(type, userId))
                .timestamp(LocalDateTime.now())
                .build());
    }

    @GetMapping("/{vehicleId}")
    public ResponseEntity<Response<VehicleResponseDTO>> getVehicleById(@PathVariable("vehicleId") String vehicleId) {
        return ResponseEntity.ok(Response.<VehicleResponseDTO>builder()
                .success(true)
                .message("Get vehicle successfully")
                .data(vehicleService.findVehicleById(vehicleId))
                .timestamp(LocalDateTime.now())
                .build());
    }

    @PutMapping("/link-project")
    public ResponseEntity<Response<Void>> linkProject(@RequestBody Map<String, String> request) {
        String vehicleId = request.get("vehicleId");
        String projectId = request.get("projectId");
        vehicleService.linkVehicleToProject(vehicleId, projectId);
        return ResponseEntity.ok(Response.<Void>builder()
                .success(true)
                .message("Link vehicle to project successfully")
                .timestamp(LocalDateTime.now())
                .build());
    }

    @PostMapping
    public ResponseEntity<Response<Void>> createVehicle(@RequestBody VehicleRequestDTO vehicleRequestDTO) {
        String userId = "user-1";
        vehicleService.createVehicle(vehicleRequestDTO, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(Response.<Void>builder()
                .success(true)
                .message("Create vehicle successfully")
                .timestamp(LocalDateTime.now())
                .build());
    }

    @PutMapping
    public ResponseEntity<Response<Void>> updateVehicle(@RequestBody VehicleRequestDTO vehicleRequestDTO) throws AccessDeniedException {
        String userId = "user-1";
        vehicleService.updateVehicle(vehicleRequestDTO, userId);
        return ResponseEntity.ok(Response.<Void>builder()
                .success(true)
                .message("Update vehicle successfully")
                .timestamp(LocalDateTime.now())
                .build());
    }

    @DeleteMapping("/{vehicleId}")
    public ResponseEntity<Response<Map<String, String>>> deleteVehicle(@PathVariable("vehicleId") String vehicleId) throws AccessDeniedException {
        String userId = "user-1";
        return ResponseEntity.ok(Response.<Map<String, String>>builder()
                .success(true)
                .message("Delete vehicle successfully")
                .data(vehicleService.deleteVehicle(vehicleId, userId))
                .timestamp(LocalDateTime.now())
                .build());
    }

    @PostMapping("/upload")
    public ResponseEntity<Response<Map<String, String>>> uploadVehicleMedia(
            @RequestParam(value = "vehicleId", required = false) String id,
            @RequestParam(value = "file") MultipartFile file
    ) {
        String userId = "user-1";
        return ResponseEntity.ok(Response.<Map<String, String>>builder()
                .success(true)
                .message("Upload vehicle image successfully")
                .data(vehicleService.uploadMedia(userId, UploadImageRequestDTO.builder()
                        .vehicleId(id)
                        .file(file)
                        .build()))
                .timestamp(LocalDateTime.now())
                .build());
    }
}
