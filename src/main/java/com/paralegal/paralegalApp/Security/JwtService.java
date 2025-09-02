package com.paralegal.paralegalApp.Security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private final SecretKey key;

    private final long expirationMs;

    public JwtService(@Value("${jwt.secret}") String base64Secret,
                      @Value("${jwt.expiration-ms}") long expirationMs){

        byte[] secretBytes = java.util.Base64.getDecoder().decode(base64Secret);
        this.key = io.jsonwebtoken.security.Keys.hmacShaKeyFor(secretBytes);
        this.expirationMs = expirationMs;
    }

    public String generateToken(String subject, Map<String, Object> claims){
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractSubject(String token){
        return parseAllClaims(token).getSubject();
    }

    public boolean isValid(String token, String expectedSubject){
        try{
            final Claims claims = parseAllClaims(token);
            return claims.getSubject().equals(expectedSubject) && claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException e){
            return false;
        }
    }

    private Claims parseAllClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
