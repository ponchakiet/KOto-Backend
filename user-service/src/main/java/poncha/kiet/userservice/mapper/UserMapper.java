package poncha.kiet.userservice.mapper;

import com.google.protobuf.Timestamp;

import org.mapstruct.*;
import poncha.kiet.userservice.dto.request.UserUpdateDTO;
import poncha.kiet.userservice.dto.response.FollowerResponse;
import poncha.kiet.userservice.dto.response.UserLandingPageResponseDTO;
import poncha.kiet.userservice.dto.response.UserProfileResponseDTO;
import poncha.kiet.userservice.dto.response.UserResponseDTO;
import poncha.kiet.userservice.entity.User;
import poncha.kiet.userservice.grpc.UserPostInfoResponse;
import poncha.kiet.userservice.projection.UserLandingPage;
import poncha.kiet.userservice.projection.UserPostInfo;
import poncha.kiet.userservice.projection.UserProfile;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    UserResponseDTO toDto(User user);

    UserProfileResponseDTO toResponseDto(UserProfile user);

    UserProfileResponseDTO toResponseDto(User user);

    UserLandingPageResponseDTO toLanding(UserLandingPage userLandingPage);

    void updateUserFromRequest(UserUpdateDTO userUpdateDTO, @MappingTarget User user);

    @Mapping(source = "createdAt", target = "createdAt")
    UserPostInfoResponse toGrpcResponse(UserPostInfo user);

    FollowerResponse toFollowerResponse(User user);

    default Timestamp map(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return Timestamp.newBuilder()
                .setSeconds(localDateTime.toEpochSecond(ZoneOffset.UTC))
                .setNanos(localDateTime.getNano())
                .build();
    }
}
