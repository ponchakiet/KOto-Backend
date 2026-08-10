package poncha.kiet.vehicleservice.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import poncha.kiet.vehicleservice.dto.request.ProjectRequestDTO;
import poncha.kiet.vehicleservice.entity.Dealer;
import poncha.kiet.vehicleservice.entity.Project;
import poncha.kiet.vehicleservice.exception.ResourceNotFoundException;
import poncha.kiet.vehicleservice.mapper.ProjectMapper;
import poncha.kiet.vehicleservice.repository.DealerRepository;
import poncha.kiet.vehicleservice.repository.ProjectRepository;
import poncha.kiet.vehicleservice.service.ProjectService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final DealerRepository dealerRepository;

    @Override
    public ProjectRequestDTO createProject(ProjectRequestDTO request) {
        Dealer dealer = dealerRepository.findById(request.getDealerId())
                .orElseThrow(() -> new ResourceNotFoundException("Not found dealer: " + request.getDealerId()));

        Project project = projectMapper.toEntity(request);
        project.setId(UUID.randomUUID().toString());
         projectRepository.save(project);
         return request;

    }
}
