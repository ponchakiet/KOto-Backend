package poncha.kiet.postservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import poncha.kiet.postservice.dto.request.PostRequestDTO;
import poncha.kiet.postservice.entity.Post;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PostMapper {
    Post toEntity(PostRequestDTO request);
}
