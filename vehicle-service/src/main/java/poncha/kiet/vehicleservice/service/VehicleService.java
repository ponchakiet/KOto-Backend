package poncha.kiet.vehicleservice.service;

import poncha.kiet.vehicleservice.dto.request.VehicleRequestDTO;
import poncha.kiet.vehicleservice.dto.response.VehicleResponseDTO;

public interface VehicleService {
    VehicleResponseDTO findVehicleById(String id);
    void createVehicle(VehicleRequestDTO vehicleRequestDTO);
}
