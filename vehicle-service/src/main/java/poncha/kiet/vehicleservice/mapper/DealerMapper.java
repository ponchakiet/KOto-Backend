package poncha.kiet.vehicleservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import poncha.kiet.vehicleservice.dto.request.DealerRequestDTO;
import poncha.kiet.vehicleservice.entity.Dealer;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DealerMapper {
    Dealer toEntity(DealerRequestDTO dealerRequestDTO);
}
