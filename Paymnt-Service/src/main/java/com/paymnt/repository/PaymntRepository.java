package com.paymnt.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.paymnt.entity.Paymnt;
import com.paymnt.enums.PaymntStatus;
public interface PaymntRepository extends JpaRepository<Paymnt,Long>{
 java.util.Optional<Paymnt> findByBookingId(Long bookingId);
 long countByPaymentStatus(PaymntStatus status);
 @org.springframework.data.jpa.repository.Query("select coalesce(sum(p.amount),0) from Paymnt p where p.paymentStatus = :status")
 Double totalByStatus(@org.springframework.data.repository.query.Param("status") PaymntStatus status);
}
