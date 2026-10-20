package com.auth.security;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
@Service
public class JwtService {
 private final Key signingKey; private final long expirationMs;
 public JwtService(@Value("${app.jwt.secret}") String secret,@Value("${app.jwt.expiration-ms:3600000}") long expirationMs){
  if(secret.length()<32)throw new IllegalArgumentException("JWT secret must contain at least 32 characters");
  signingKey=Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));this.expirationMs=expirationMs;
 }
 public String createToken(String username,String role,Long customerId){
  Date now=new Date();
  return Jwts.builder().subject(username).claim("role",role).claim("customerId", customerId == null ? "" : customerId).issuedAt(now).expiration(new Date(now.getTime()+expirationMs)).signWith(signingKey).compact();
 }
}
