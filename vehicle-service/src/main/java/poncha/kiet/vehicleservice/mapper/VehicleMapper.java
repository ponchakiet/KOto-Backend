package poncha.kiet.vehicleservice.mapper;

import org.mapstruct.*;
import poncha.kiet.vehicleservice.dto.request.VehicleRequestDTO;
import poncha.kiet.vehicleservice.dto.response.VehicleResponseDTO;
import poncha.kiet.vehicleservice.entity.Vehicle;
import poncha.kiet.vehicleservice.grpc.VehicleRequest;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface VehicleMapper {
    VehicleResponseDTO toDto(Vehicle vehicle);

    void update(VehicleRequestDTO request, @MappingTarget Vehicle vehicle);

    Vehicle toEntity(VehicleRequestDTO vehicleRequestDTO);
}
