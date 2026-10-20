package com.booking.config;
import org.springframework.context.annotation.*;
import org.springframework.web.context.request.*;
import feign.RequestInterceptor;
@Configuration
public class FeignCorrelationConfig {
 @Bean RequestInterceptor correlationInterceptor(){
  return template -> {
   String id=null;
   try{var attrs=RequestContextHolder.getRequestAttributes();
     if(attrs instanceof ServletRequestAttributes s) id=s.getRequest().getHeader("X-Correlation-Id");
   }catch(Exception ignored){}
   if(id!=null&&!id.isBlank())template.header("X-Correlation-Id",id);
  };
 }
}
