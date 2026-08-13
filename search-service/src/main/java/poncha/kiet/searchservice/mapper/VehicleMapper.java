package poncha.kiet.searchservice.mapper;

import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import poncha.kiet.searchservice.entity.VehicleDocument;
import poncha.kiet.searchservice.grpc.VehicleResponse;
import poncha.kiet.searchservice.mapper.decorator.VehicleMapperDecorator;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
@DecoratedWith(VehicleMapperDecorator.class)
public interface VehicleMapper {
    VehicleDocument toDocument(VehicleResponse response);
}
