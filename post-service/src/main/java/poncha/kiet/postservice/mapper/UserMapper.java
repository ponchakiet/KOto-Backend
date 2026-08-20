package poncha.kiet.postservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import poncha.kiet.postservice.dto.response.UserPostInfoResponseDTO;

import com.google.protobuf.Timestamp;
import poncha.kiet.postservice.grpc.UserPostInfoResponse;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    UserPostInfoResponseDTO toResponseDTO(UserPostInfoResponse user);

    default LocalDateTime map(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return LocalDateTime.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos(), ZoneOffset.UTC);
    }
}
