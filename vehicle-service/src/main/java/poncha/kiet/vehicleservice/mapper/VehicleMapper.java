package poncha.kiet.vehicleservice.mapper;

import org.apache.hc.core5.function.Decorator;
import org.mapstruct.*;
import poncha.kiet.vehicleservice.dto.request.VehicleRequestDTO;
import poncha.kiet.vehicleservice.dto.response.VehicleResponseDTO;
import poncha.kiet.vehicleservice.dto.response.VehicleSummaryResponseDTO;
import poncha.kiet.vehicleservice.entity.Vehicle;
import poncha.kiet.vehicleservice.grpc.VehicleResponse;
import poncha.kiet.vehicleservice.mapper.decorator.VehicleMapperDecorator;
import poncha.kiet.vehicleservice.projection.VehicleSummary;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
@DecoratedWith(VehicleMapperDecorator.class)
public interface VehicleMapper {
    VehicleResponseDTO toDto(Vehicle vehicle);

    @Mapping(target = "properties", ignore = true)
    VehicleResponse toResponse(Vehicle vehicle);

    VehicleSummaryResponseDTO toSummary(VehicleSummary vehicleSummary);

    void update(VehicleRequestDTO request, @MappingTarget Vehicle vehicle);

    Vehicle toEntity(VehicleRequestDTO vehicleRequestDTO);
}
