package poncha.kiet.vehicleservice.service;

import poncha.kiet.vehicleservice.dto.request.UploadImageRequestDTO;
import poncha.kiet.vehicleservice.dto.request.VehicleRequestDTO;
import poncha.kiet.vehicleservice.dto.response.VehicleResponseDTO;

import java.util.Map;

public interface VehicleService {
    VehicleResponseDTO findVehicleById(String id);
    void createVehicle(VehicleRequestDTO vehicleRequestDTO);
    void linkVehicleToProject(String vehicleId, String projectId);
    Map<String, String> uploadImage(UploadImageRequestDTO request);
    void updateVehicle(VehicleRequestDTO request);
}
