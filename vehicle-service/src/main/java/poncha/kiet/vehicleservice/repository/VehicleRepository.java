package poncha.kiet.vehicleservice.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import poncha.kiet.vehicleservice.entity.Vehicle;

@Repository
public interface VehicleRepository extends MongoRepository<Vehicle,String> {
}
