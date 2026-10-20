package com.notification.service;
import java.util.List;
import com.notification.dto.*;
public interface NotificationService {
 NotificationResponseDto create(NotificationRequestDto request);
 List<NotificationResponseDto> byCustomer(Long customerId);
 NotificationResponseDto markRead(Long id);
 NotificationResponseDto get(Long id);
}
