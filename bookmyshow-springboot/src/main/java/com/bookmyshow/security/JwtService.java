package com.bookmyshow.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Service
public class JwtService {
  private final SecretKey key;
  private final long expiration;

  public JwtService(@Value("${app.jwt.secret}") String secret,
      @Value("${app.jwt.expiration-ms}") long expiration) {
    this.key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
    this.expiration = expiration;
  }

  public String generate(String subject) {
    Date now = new Date();
    return Jwts.builder().subject(subject).issuedAt(now)
        .expiration(new Date(now.getTime() + expiration)).signWith(key).compact();
  }

  public String extractSubject(String token) {
    return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject();
  }
}
