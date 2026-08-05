package poncha.kiet.postservice.mapper;

import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import poncha.kiet.postservice.dto.response.VehicleDetailResponseDTO;
import poncha.kiet.postservice.grpc.VehicleResponse;
import poncha.kiet.postservice.mapper.decorator.VehicleMapperDecorator;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
@DecoratedWith(value = VehicleMapperDecorator.class)
public interface VehicleMapper {
    VehicleDetailResponseDTO toDto(VehicleResponse vehicle);
}
