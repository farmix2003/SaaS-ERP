package farmix.com.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration-ms}")
    private long accessTokenExpirationMs;

    public String generateAccessToken(UserPrincipal userPrincipal) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + accessTokenExpirationMs);

        return Jwts.builder()
                .subject(userPrincipal.getUsername())
                .claim("userId",userPrincipal.getId())
                .claim("companyId", userPrincipal.getCompanyId())
                .claim("role", userPrincipal.getRole())
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(getSigningKey())
                .compact();
    }

    public String extractEmail(String token){
        return extractClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, UserPrincipal userPrincipal) {
        String email = extractEmail(token);

        return email.equals(userPrincipal.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaims(token)
                .getExpiration()
                .before(new Date());
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
      byte[] keyBytes = Base64.getDecoder().decode(secret);
      return Keys.hmacShaKeyFor(keyBytes);
    }

}
