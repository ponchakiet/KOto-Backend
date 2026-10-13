package poncha.kiet.paymentservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import poncha.kiet.paymentservice.entity.Payment;

import java.time.LocalDateTime;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
    @Query("""
                SELECT COALESCE(SUM(t.amount), 0)
                FROM Payment t
                WHERE t.status = 'SUCCESS'
                  AND t.createdAt >= :start
                  AND t.createdAt < :end
            """)
    Double sumSuccessfulTransactions(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
