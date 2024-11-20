package com.software.upskilled.utils;

import com.software.upskilled.Entity.Users;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Service class for JWT (JSON Web Token) operations.
 * This class handles token generation, validation, and claim extraction.
 */
@Service
public class JWTUtil {
    private String secret;
    private long EXPIRATION_TIME = 1000 * 60 * 60 * 24; // 1 day

    @Value("${jwt.secret}")
    public void setSecret(String secret) {
        this.secret = secret;
    }

    /**
     * Generates a JWT (JSON Web Token) for the provided user.
     *
     * This method creates a JWT by embedding user-specific claims such as first name, last name, role, status, and designation
     * (if available). It then generates the token using the user's email as the subject and the provided claims.
     *
     * @param user The user for whom the JWT token is being generated.
     * @return A JWT token string containing the user's details as claims.
     */
    public String generateToken(Users user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("firstName", user.getFirstName());
        claims.put("lastName", user.getLastName());
        claims.put("role", user.getRole());
        claims.put("status", user.getStatus());
        // Include designation only if it’s not null
        if (user.getDesignation() != null) {
            claims.put("designation", user.getDesignation());
        }
        return createToken(claims, user.getEmail());
    }

    /**
     * Creates a JWT (JSON Web Token) based on the provided claims and email.
     *
     * This method builds a JWT using the provided claims and the user's email as the subject. It sets the issued time and
     * expiration time, then signs the token with the specified secret using the HS256 algorithm. The final token is then
     * returned as a compact string representation.
     *
     * @param claims A map containing the claims to be embedded in the token (e.g., user roles, status).
     * @param email The email of the user, which is set as the subject of the token.
     * @return A compact JWT token as a string.
     */
    private String createToken(Map<String, Object> claims, String email) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    /**
     * Validates the JWT token by checking if the username matches and if the token is not expired.
     *
     * This method extracts the username from the provided token and compares it with the given username.
     * It also checks if the token has expired. The token is considered valid if both the username matches
     * and the token has not expired.
     *
     * @param token The JWT token to be validated.
     * @param username The username to be compared with the token’s extracted username.
     * @return True if the token is valid (username matches and the token is not expired), otherwise false.
     */
    public Boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    /**
     * Extracts the username (subject) from the JWT token.
     *
     * This method retrieves the subject (which typically represents the username) from the provided JWT token.
     * It calls a helper method `extractAllClaims` to parse and extract the claims before returning the subject.
     *
     * @param token The JWT token from which the username will be extracted.
     * @return The username (subject) stored in the token's claims.
     */
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Extracts all claims from the provided JWT token.
     *
     * This method parses the JWT token using the signing key and retrieves the claims (body) of the token.
     * The claims may contain user-specific information, including the subject, roles, and other custom fields.
     *
     * @param token The JWT token from which the claims will be extracted.
     * @return The claims extracted from the token.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(secret).build().parseClaimsJws(token).getBody();
    }

    /**
     * Checks if the given JWT token has expired.
     *
     * @param token the JWT token to be checked.
     * @return true if the token's expiration date is before the current date, indicating it has expired;
     *         false otherwise.
     */
    private Boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }
}
