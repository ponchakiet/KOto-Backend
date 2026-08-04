package poncha.kiet.vehicleservice.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import poncha.kiet.vehicleservice.entity.Dealer;

@Repository
public interface DealerRepository extends MongoRepository<Dealer, String> {
}
