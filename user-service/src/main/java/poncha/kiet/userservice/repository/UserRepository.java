package poncha.kiet.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import poncha.kiet.userservice.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User,String> {
}
