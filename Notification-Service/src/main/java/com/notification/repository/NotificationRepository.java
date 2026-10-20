package com.notification.repository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.notification.entity.Notification;
public interface NotificationRepository extends JpaRepository<Notification,Long>{
 List<Notification> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
 List<Notification> findByCustomerIdAndNotificationStatusOrderByCreatedAtDesc(Long customerId, com.notification.enums.NotificationStatus status);
 long countByNotificationStatus(com.notification.enums.NotificationStatus status);
}
