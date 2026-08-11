package poncha.kiet.vehicleservice.mapper.decorator;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import poncha.kiet.vehicleservice.dto.response.CategoryResponseDTO;
import poncha.kiet.vehicleservice.dto.response.VehicleResponseDTO;
import poncha.kiet.vehicleservice.dto.response.VehicleSummaryResponseDTO;
import poncha.kiet.vehicleservice.entity.*;
import poncha.kiet.vehicleservice.exception.ResourceNotFoundException;
import poncha.kiet.vehicleservice.grpc.CategoryResponse;
import poncha.kiet.vehicleservice.grpc.Content;
import poncha.kiet.vehicleservice.grpc.VehicleResponse;
import poncha.kiet.vehicleservice.mapper.VehicleMapper;
import poncha.kiet.vehicleservice.projection.VehicleSummary;
import poncha.kiet.vehicleservice.repository.CategoryRepository;

import java.util.Arrays;
import java.util.List;

public abstract class VehicleMapperDecorator implements VehicleMapper {
    @Autowired
    private VehicleMapper vehicleMapper;
    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public VehicleSummaryResponseDTO toSummary(VehicleSummary vehicleSummary) {
        VehicleSummaryResponseDTO response = vehicleMapper.toSummary(vehicleSummary);
        String url = vehicleSummary.getContents().stream()
                .filter(c -> c instanceof Image)
                .map(c -> (Image) c)
                .findFirst().get().getUrl();
        response.setImageUrl(url);
        response.setTotalImages(vehicleSummary.getContents().size());

        return response;
    }

    @Override
    public VehicleResponseDTO toDto(Vehicle vehicle) {
        VehicleResponseDTO response = vehicleMapper.toDto(vehicle);
        if (vehicle.getCategoryId() != null) {
            Category category = categoryRepository.findById(vehicle.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Not found category"));

            response.setCategory(CategoryResponseDTO.builder()
                    .id(category.getId())
                    .name(category.getName())
                    .build());
        }

        return response;
    }

    @Override
    public VehicleResponse toResponse(Vehicle vehicle) {
        VehicleResponse.Builder response = vehicleMapper.toResponse(vehicle).toBuilder();

        if (vehicle.getProperties() != null) {
            response.putAllProperties(vehicle.getProperties());
        }

        if (vehicle.getCategoryId() != null) {
            CategoryResponse categoryResponse = categoryRepository.findById(vehicle.getCategoryId())
                    .map(c -> CategoryResponse.newBuilder()
                            .setId(c.getId())
                            .setName(c.getName())
                            .build())
                    .get();

            response.setCategory(categoryResponse);
        }

        if (vehicle.getOtherInfo() != null) {
            response.addAllOtherInfo(Arrays.asList(vehicle.getOtherInfo()));
        }

        List<Content> contentList = vehicle.getContents().stream()
                .map(c -> {
                    Content.Builder builder = Content.newBuilder();

                    if (c instanceof Video video) {
                        builder.setId(video.getId())
                                .setDuration(video.getDuration())
                                .setName(video.getName())
                                .setType(ContentType.VIDEO.name())
                                .setUrl(video.getUrl());
                    } else if (c instanceof Image image) {
                        builder.setId(image.getId())
                                .setDuration(0) // or Duration.getDefaultInstance() if it's a message
                                .setName(image.getName())
                                .setType(ContentType.IMAGE.name())
                                .setUrl(image.getUrl());
                    } else {
                        throw new IllegalArgumentException("Unknown content type: " + c.getClass());
                    }
                    return builder.build();
                }).toList();
        response.addAllContentList(contentList);

        return response.build();
    }
}
