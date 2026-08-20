package poncha.kiet.postservice.mapper;

import org.mapstruct.*;
import poncha.kiet.postservice.dto.request.PostRequestDTO;
import poncha.kiet.postservice.dto.response.*;
import poncha.kiet.postservice.entity.Post;
import poncha.kiet.postservice.grpc.PostDetailResponse;
import poncha.kiet.postservice.mapper.decorator.PostMapperDecorator;
import poncha.kiet.postservice.projection.PostHistory;
import poncha.kiet.postservice.projection.PostOverview;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {VehicleMapper.class})
@DecoratedWith(PostMapperDecorator.class)
public interface PostMapper {
    Post toEntity(PostRequestDTO request);

    void update(PostRequestDTO request, @MappingTarget Post post);

    PostDetailResponseDTO toDetailResponse(Post post);

    PostOverviewResponseDTO toOverview(PostOverview postOverview);

    PostWebmasterOverviewResponseDTO toWebmasterResponse(PostOverview postOverview);

    PostHistoryResponseDTO toHistory(PostHistory postHistory);

    PostResponseDTO toDto(Post post);

    PostDetailResponse toDetail(Post post);
}
