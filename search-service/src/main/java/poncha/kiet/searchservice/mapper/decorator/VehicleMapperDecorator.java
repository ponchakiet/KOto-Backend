package poncha.kiet.searchservice.mapper.decorator;

import org.springframework.beans.factory.annotation.Autowired;
import poncha.kiet.searchservice.entity.VehicleDocument;
import poncha.kiet.searchservice.entity.enums.ContentType;
import poncha.kiet.searchservice.grpc.VehicleResponse;
import poncha.kiet.searchservice.mapper.VehicleMapper;

public abstract class VehicleMapperDecorator implements VehicleMapper {
    @Autowired
    private VehicleMapper vehicleMapper;

    @Override
    public VehicleDocument toDocument(VehicleResponse response) {
        VehicleDocument vehicleDocument = vehicleMapper.toDocument(response);
        if (response.getContentListList() != null) {
            VehicleDocument.MediaDocument mediaDocument = response.getContentListList().stream()
                    .filter(c -> ContentType.IMAGE.name().equals(c.getType()))
                    .findFirst()
                    .map(c -> VehicleDocument.MediaDocument.builder()
                            .url(c.getUrl())
                            .id(c.getId())
                            .build())
                    .get();
            vehicleDocument.setMedia(mediaDocument);
        }

        vehicleDocument.setOtherInfo(response.getOtherInfoList()
                .stream()
                .map(String::valueOf)
                .toArray(String[]::new));

        return vehicleDocument;
    }
}
