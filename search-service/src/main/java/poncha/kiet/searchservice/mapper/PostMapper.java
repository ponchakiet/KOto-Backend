package poncha.kiet.searchservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import poncha.kiet.searchservice.entity.PostDocument;
import poncha.kiet.searchservice.grpc.PostDetailResponse;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {VehicleMapper.class})
public interface PostMapper {
    PostDocument toDocument(PostDetailResponse response);
}
