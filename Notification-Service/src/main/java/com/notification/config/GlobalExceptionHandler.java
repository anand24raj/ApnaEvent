package com.notification.config;
import java.time.LocalDateTime;
import java.util.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.slf4j.MDC;
@RestControllerAdvice
public class GlobalExceptionHandler {
 @ExceptionHandler(Exception.class)
 public ResponseEntity<Map<String,Object>> handle(Exception ex,HttpServletRequest req){
  Map<String,Object> body=new LinkedHashMap<>();
  body.put("timestamp",LocalDateTime.now());body.put("status",400);body.put("error","Bad Request");
  body.put("message",ex.getMessage());body.put("path",req.getRequestURI());body.put("correlationId",MDC.get("correlationId"));
  return ResponseEntity.badRequest().body(body);
 }
}
