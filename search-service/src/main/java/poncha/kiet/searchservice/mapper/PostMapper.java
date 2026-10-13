package poncha.kiet.searchservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import poncha.kiet.searchservice.entity.PostDocument;
import poncha.kiet.searchservice.grpc.PostDetailResponse;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {VehicleMapper.class})
public interface PostMapper {
    PostDocument toDocument(PostDetailResponse response);

    default LocalDateTime map(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return LocalDateTime.parse(value);
    }

}
