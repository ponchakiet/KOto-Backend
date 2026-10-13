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

    default UserPostInfoResponse toGrpcResponse(UserPostInfo user) {
        UserPostInfoResponse.Builder builder = UserPostInfoResponse.newBuilder();

        if (user.getId() != null)
            builder.setId(user.getId());

        if (user.getFirstName() != null)
            builder.setFirstName(user.getFirstName());

        if (user.getLastName() != null)
            builder.setLastName(user.getLastName());

        if (user.getAvtUrl() != null)
            builder.setAvtUrl(user.getAvtUrl());

        if (user.getEmail() != null)
            builder.setEmail(user.getEmail());

        if (user.getPhoneNumber() != null)
            builder.setPhoneNumber(user.getPhoneNumber());

        if (user.getCreatedAt() != null)
            builder.setCreatedAt(map(user.getCreatedAt()));

        if (user.getLastAccess() != null)
            builder.setLastAccess(map(user.getLastAccess()));

        return builder.build();
    }

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
