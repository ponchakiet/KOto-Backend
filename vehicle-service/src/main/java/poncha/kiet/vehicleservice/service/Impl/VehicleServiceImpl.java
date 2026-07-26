package poncha.kiet.vehicleservice.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import poncha.kiet.vehicleservice.exception.ResourceNotFoundException;
import poncha.kiet.vehicleservice.dto.request.VehicleRequestDTO;
import poncha.kiet.vehicleservice.dto.response.VehicleResponseDTO;
import poncha.kiet.vehicleservice.entity.Vehicle;
import poncha.kiet.vehicleservice.mapper.VehicleMapper;
import poncha.kiet.vehicleservice.repository.VehicleRepository;
import poncha.kiet.vehicleservice.service.VehicleService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {
    private final VehicleRepository vehicleRepository;
    private final VehicleMapper vehicleMapper;

    @Override
    public VehicleResponseDTO findVehicleById(String id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle Not Found"));

        return vehicleMapper.toDto(vehicle);
    }

    @Override
    public void createVehicle(VehicleRequestDTO vehicleRequestDTO) {
        Vehicle vehicle = vehicleMapper.toEntity(vehicleRequestDTO);
        vehicle.setId(UUID.randomUUID().toString());
        vehicleRepository.save(vehicle);
    }
}
