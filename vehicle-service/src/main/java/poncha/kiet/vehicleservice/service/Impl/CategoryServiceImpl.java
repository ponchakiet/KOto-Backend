package poncha.kiet.vehicleservice.service.Impl;

import jakarta.ws.rs.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import poncha.kiet.vehicleservice.dto.response.CategoryResponseDTO;
import poncha.kiet.vehicleservice.entity.Category;
import poncha.kiet.vehicleservice.mapper.CategoryMapper;
import poncha.kiet.vehicleservice.repository.CategoryRepository;
import poncha.kiet.vehicleservice.service.CategoryService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public Category create(Map<String, String> category) {
        if (!category.containsKey("name"))
            throw new BadRequestException("Missing name category");

        Category entity = Category.builder()
                .id(UUID.randomUUID().toString())
                .name(category.get("name"))
                .build();

        return categoryRepository.save(entity);
    }

    @Override
    public List<CategoryResponseDTO> findAll() {
        List<Category> categories = categoryRepository.findAll();

        return categories.stream().map(categoryMapper::toDto).toList();
    }
}
