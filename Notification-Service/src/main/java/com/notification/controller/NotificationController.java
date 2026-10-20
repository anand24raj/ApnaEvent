package com.notification.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import com.notification.dto.*;
import com.notification.service.NotificationService;

@RestController
@RequestMapping("/notification")
public class NotificationController {
    private final NotificationService service;
    public NotificationController(NotificationService service){this.service=service;}

    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestBody NotificationRequestDto r){
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(r));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<?> byCustomer(@PathVariable Long customerId, jakarta.servlet.http.HttpServletRequest request){
        if ("CUSTOMER".equals(request.getHeader("X-User-Role")) && !same(customerId, request))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(java.util.Map.of("message","You can only view your own notifications."));
        return ResponseEntity.ok(service.byCustomer(customerId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id){return ResponseEntity.ok(service.get(id));}

    @PatchMapping("/{id}/read")
    public ResponseEntity<?> read(@PathVariable Long id){return ResponseEntity.ok(service.markRead(id));}

    private boolean same(Long id, jakarta.servlet.http.HttpServletRequest request){
        try { return id != null && id.equals(Long.valueOf(request.getHeader("X-Customer-Id"))); }
        catch(Exception e){ return false; }
    }
}
