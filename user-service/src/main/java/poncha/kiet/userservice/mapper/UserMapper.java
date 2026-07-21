package poncha.kiet.userservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import poncha.kiet.userservice.dto.response.UserResponseDTO;
import poncha.kiet.userservice.entity.User;

@Mapper(componentModel = "spring", uses = UserMapper.class, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    UserResponseDTO toDto(User user);
}
