package poncha.kiet.postservice.mapper.decorator;

import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import poncha.kiet.postservice.dto.response.VehicleDetailResponseDTO;
import poncha.kiet.postservice.grpc.VehicleResponse;
import poncha.kiet.postservice.mapper.VehicleMapper;

@NoArgsConstructor
public class VehicleMapperDecorator implements VehicleMapper {
    @Autowired
    private VehicleMapper vehicleMapper;

    @Override
    public VehicleDetailResponseDTO toDto(VehicleResponse vehicle) {
        VehicleDetailResponseDTO vehicleDetailResponseDTO = vehicleMapper.toDto(vehicle);

        vehicleDetailResponseDTO.setContents(
                vehicle.getContentListList().stream()
                        .map(c -> VehicleDetailResponseDTO.Content.builder()
                                .url(c.getUrl())
                                .id(c.getId())
                                .name(c.getName())
                                .duration(c.getDuration())
                                .type(c.getType())
                                .build()).toList()
        );
        return vehicleDetailResponseDTO;
    }
}
