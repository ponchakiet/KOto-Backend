package poncha.kiet.vehicleservice.mapper.decorator;

import org.springframework.beans.factory.annotation.Autowired;
import poncha.kiet.vehicleservice.dto.response.CategoryResponseDTO;
import poncha.kiet.vehicleservice.dto.response.VehicleResponseDTO;
import poncha.kiet.vehicleservice.dto.response.VehicleSummaryResponseDTO;
import poncha.kiet.vehicleservice.entity.Category;
import poncha.kiet.vehicleservice.entity.Image;
import poncha.kiet.vehicleservice.entity.Vehicle;
import poncha.kiet.vehicleservice.exception.ResourceNotFoundException;
import poncha.kiet.vehicleservice.mapper.VehicleMapper;
import poncha.kiet.vehicleservice.projection.VehicleSummary;
import poncha.kiet.vehicleservice.repository.CategoryRepository;

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
}
