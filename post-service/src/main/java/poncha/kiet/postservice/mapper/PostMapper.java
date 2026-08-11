package poncha.kiet.postservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import poncha.kiet.postservice.dto.request.PostRequestDTO;
import poncha.kiet.postservice.dto.response.PostHistoryResponseDTO;
import poncha.kiet.postservice.dto.response.PostOverviewResponseDTO;
import poncha.kiet.postservice.dto.response.PostResponseDTO;
import poncha.kiet.postservice.entity.Post;
import poncha.kiet.postservice.projection.PostHistory;
import poncha.kiet.postservice.projection.PostOverview;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {VehicleMapper.class})
public interface PostMapper {
    Post toEntity(PostRequestDTO request);

    void update(PostRequestDTO request, @MappingTarget Post post);

    PostOverviewResponseDTO toOverview(PostOverview postOverview);

    PostHistoryResponseDTO toHistory(PostHistory postHistory);

    PostResponseDTO toDto(Post post);
}
