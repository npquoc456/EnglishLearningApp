package com.example.demo.security;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
@Service
@RequiredArgsConstructor
public class JwtIssuer {
    private final JwtEncoder encoder;
    public String issue(Long userId) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer("flashcardvn-api")
                .subject(userId.toString()).issuedAt(now)
                .expiresAt(now.plus(7, ChronoUnit.DAYS)).build();
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(),claims)).getTokenValue();
    }
}
