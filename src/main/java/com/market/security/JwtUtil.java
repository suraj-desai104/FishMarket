package com.market.security;


import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private final String SECRET = "mysupersecretkeymysupersecretkey12345"; // >=32 chars
    private final long EXPIRATION = 1000 * 60 * 60; // 1 hour

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    // Generate JWT token
 // Generate JWT token with username and role
    public String generateToken(String username, String role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role) // add role to token
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public String extractRole(String token) {
        return (String) Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("role");
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
    
    public String generateResetPasswordToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .claim("resetPassword", true)  // Custom claim to identify it's for password reset
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 5 * 60 * 1000)) // 5 min expiry
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // ✅ Correct signing key
                .compact();
    }

    
    public boolean validateResetPasswordToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // ✅ Check that token has not expired
            boolean notExpired = claims.getExpiration().after(new Date());

            // ✅ Check that this is specifically a password reset token
            boolean isResetToken = Boolean.TRUE.equals(claims.get("resetPassword", Boolean.class));

            return notExpired && isResetToken;
        } catch (JwtException | IllegalArgumentException e) {
            // Token is either invalid or expired
            return false;
        }
    }


    
 // ✅ Extract username only if it's a reset password token
    public String extractUsernameFromResetToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // Check if resetPassword claim is present and true
            Boolean isResetToken = claims.get("resetPassword", Boolean.class);
            if (isResetToken != null && isResetToken) {
                return claims.getSubject(); // ✅ return the username from "sub"
            } else {
                throw new RuntimeException("Token is not a valid reset password token");
            }

        } catch (JwtException e) {
            throw new RuntimeException("Invalid or expired token");
        }
    }


}
