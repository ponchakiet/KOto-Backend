package poncha.kiet.userservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import poncha.kiet.userservice.dto.request.UserUpdateDTO;
import poncha.kiet.userservice.dto.response.FollowerResponse;
import poncha.kiet.userservice.dto.response.UserResponseDTO;
import poncha.kiet.userservice.entity.User;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    UserResponseDTO toDto(User user);

    void updateUserFromRequest(UserUpdateDTO userUpdateDTO, @MappingTarget User user);

    FollowerResponse toFollowerResponse(User user);
}
