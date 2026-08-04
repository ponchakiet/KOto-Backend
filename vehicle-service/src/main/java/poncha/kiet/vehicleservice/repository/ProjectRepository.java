package poncha.kiet.vehicleservice.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import poncha.kiet.vehicleservice.entity.Project;

@Repository
public interface ProjectRepository extends MongoRepository<Project,String> {
}
