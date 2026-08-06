package poncha.kiet.vehicleservice.service;

import poncha.kiet.vehicleservice.dto.response.CategoryResponseDTO;
import poncha.kiet.vehicleservice.entity.Category;

import java.util.List;
import java.util.Map;

public interface CategoryService {
    Category create(Map<String, String> category);

    List<CategoryResponseDTO> findAll();
}
