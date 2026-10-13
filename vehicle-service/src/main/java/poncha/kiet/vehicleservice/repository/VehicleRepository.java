package poncha.kiet.vehicleservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import poncha.kiet.vehicleservice.entity.Vehicle;
import poncha.kiet.vehicleservice.entity.VehicleType;
import poncha.kiet.vehicleservice.projection.VehicleSummary;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VehicleRepository extends MongoRepository<Vehicle, String> {
    Page<VehicleSummary> findAllByUserIdAndType(String userId, VehicleType type, Pageable pageable);

    List<Vehicle> findAllByTypeAndUserId(VehicleType type, String userId);

    @Query(value = "{ 'type': 'PERSIST', " + "'createdAt': { $gte: ?0, $lt: ?1 } }", count = true)
    long countVehiclePERSIST(LocalDateTime start, LocalDateTime end);
}
