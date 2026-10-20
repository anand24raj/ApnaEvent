package com.notification.service;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import com.notification.dto.*;
import com.notification.entity.Notification;
import com.notification.enums.NotificationStatus;
import com.notification.repository.NotificationRepository;
@Service
public class NotificationServiceImp implements NotificationService {
 private final NotificationRepository repo;
 public NotificationServiceImp(NotificationRepository repo){this.repo=repo;}
 public NotificationResponseDto create(NotificationRequestDto request){
  Notification n=new Notification();BeanUtils.copyProperties(request,n);
  n.setNotificationStatus(NotificationStatus.PENDING);n.setCreatedAt(LocalDateTime.now());
  return toResponse(repo.save(n));
 }
 public List<NotificationResponseDto> byCustomer(Long customerId){return repo.findByCustomerIdOrderByCreatedAtDesc(customerId).stream().map(this::toResponse).toList();}
 public NotificationResponseDto markRead(Long id){Notification n=repo.findById(id).orElseThrow(()->new IllegalArgumentException("Notification not found: "+id));n.setNotificationStatus(NotificationStatus.READ);return toResponse(repo.save(n));}
 public NotificationResponseDto get(Long id){return toResponse(repo.findById(id).orElseThrow(()->new IllegalArgumentException("Notification not found: "+id)));}
 private NotificationResponseDto toResponse(Notification n){NotificationResponseDto r=NotificationResponseDto.builder().build();BeanUtils.copyProperties(n,r);return r;}
}
