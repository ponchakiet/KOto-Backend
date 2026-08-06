package poncha.kiet.vehicleservice.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import poncha.kiet.vehicleservice.entity.Category;

@Repository
public interface CategoryRepository extends MongoRepository<Category,String> {
}
