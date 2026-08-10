package poncha.kiet.vehicleservice.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import poncha.kiet.vehicleservice.dto.request.UploadImageRequestDTO;
import poncha.kiet.vehicleservice.dto.request.VehicleRequestDTO;
import poncha.kiet.vehicleservice.dto.response.VehicleResponseDTO;
import poncha.kiet.vehicleservice.dto.response.VehicleSummaryResponseDTO;

import java.util.List;
import java.util.Map;

public interface VehicleService {
    VehicleResponseDTO findVehicleById(String id);

    List<VehicleResponseDTO> findVehicleByType(String type, String userId);

    Page<VehicleSummaryResponseDTO> findAll(String userId, Pageable pageable);

    void createVehicle(VehicleRequestDTO vehicleRequestDTO);

    void linkVehicleToProject(String vehicleId, String projectId);

    Map<String, String> uploadImage(UploadImageRequestDTO request);

    void updateVehicle(VehicleRequestDTO request);
}
