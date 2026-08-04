package poncha.kiet.vehicleservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import poncha.kiet.vehicleservice.dto.request.ProjectRequestDTO;
import poncha.kiet.vehicleservice.dto.response.Response;
import poncha.kiet.vehicleservice.service.ProjectService;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;
    @PostMapping
    public ResponseEntity<Response<Void>> createProject(@RequestBody ProjectRequestDTO request) {
        projectService.createProject(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Response.<Void>builder()
                .success(true)
                .message("Create project successfully")
                .timestamp(LocalDateTime.now())
                .build());
    }
}
