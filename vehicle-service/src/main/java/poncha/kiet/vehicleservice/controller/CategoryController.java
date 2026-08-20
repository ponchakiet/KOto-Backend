package poncha.kiet.vehicleservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poncha.kiet.vehicleservice.dto.response.CategoryResponseDTO;
import poncha.kiet.vehicleservice.dto.response.Response;
import poncha.kiet.vehicleservice.entity.Category;
import poncha.kiet.vehicleservice.service.CategoryService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;
    @PostMapping("/categories")
    public ResponseEntity<Response<Category>> createCategory(@RequestBody Map<String, String> category) {
        return ResponseEntity.status(HttpStatus.CREATED).body(Response.<Category>builder()
                .success(true)
                .message("Create category successfully")
                .data(categoryService.create(category))
                .timestamp(LocalDateTime.now())
                .build());
    }

    @GetMapping("/public/categories")
    public ResponseEntity<Response<List<CategoryResponseDTO>>> getAllCategories() {
        return ResponseEntity.ok(Response.<List<CategoryResponseDTO>>builder()
                .success(true)
                .message("Get all categories successfully")
                .data(categoryService.findAll())
                .timestamp(LocalDateTime.now())
                .build());
    }
}

