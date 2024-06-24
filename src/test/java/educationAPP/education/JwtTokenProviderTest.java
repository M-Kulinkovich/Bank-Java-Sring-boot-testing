package educationAPP.education;

import educationAPP.education.config.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Key;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    public void setUp() {
        String secretKey = "mySecretKeyForJwtTokenShouldBeLongEnoughToBeSecure";
        long validityInMilliseconds = 3600000;
        jwtTokenProvider = new JwtTokenProvider(secretKey, validityInMilliseconds);
    }

    private Key key() {
        byte[] bytes = Decoders.BASE64.decode(jwtTokenProvider.getJwtSecret());
        return Keys.hmacShaKeyFor(bytes);
    }

    @Test
    public void testGenerateToken() {
        UserDetails userDetails = new User("testuser", "password", Collections.emptyList());
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null);
        String token = jwtTokenProvider.generateToken(authentication);
        Claims claims = Jwts.parser()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertEquals("testuser", claims.getSubject());
    }

    @Test
    public void testValidateToken_Valid() {
        String token = jwtTokenProvider.generateToken(createAuthentication());
        boolean isValid = jwtTokenProvider.validateToken(token);

        assertTrue(isValid);
    }

    @Test
    public void testValidateToken_Invalid() {
        String invalidToken = "invalid.token";
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        assertFalse(isValid);
    }

    @Test
    public void testGetUsernameFromToken() {
        String token = jwtTokenProvider.generateToken(createAuthentication());
        String username = jwtTokenProvider.getUsername(token);

        assertEquals("testuser", username);
    }

    private Authentication createAuthentication() {
        UserDetails userDetails = new User("testuser", "password", Collections.emptyList());
        return new UsernamePasswordAuthenticationToken(userDetails, null);
    }
}
