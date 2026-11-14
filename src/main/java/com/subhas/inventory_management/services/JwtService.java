package com.subhas.inventory_management.services;

import com.subhas.inventory_management.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JwtService {
    @Value("${jwt.secret}") // Put a long, random string in application.properties
    private String secret;

    @Value("${jwt.expiration}")
    private long expirationMs;

    public String generateToken(User user) {
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("role", user.getRole()) // Embed role for frontend use
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs)) // 10 hours
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parser().verifyWith(Keys.hmacShaKeyFor(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                .build().parseSignedClaims(token).getPayload().getSubject();
    }

    public boolean isTokenValid(String token, org.springframework.security.core.userdetails.UserDetails user) {
        try {
            String username = extractUsername(token);
            java.util.Date exp = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                    .build().parseSignedClaims(token).getPayload().getExpiration();
            return username.equals(user.getUsername()) && exp.after(new java.util.Date());
        } catch (Exception e) {
            return false;
        }
    }

    public String extractRole(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
    }
}
