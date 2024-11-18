package com.software.upskilled.utils;

import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.Set;

/**
 * class for managing a token blacklist.
 * This class provides functionality to blacklist tokens and check if a token is blacklisted.
 */
@Service
public class TokenBlackListService {

    private final Set<String> tokenBlacklist = new HashSet<>(); // Replace with Redis in production

    public void blacklistToken(String token) {
        tokenBlacklist.add(token);
    }

    public boolean isTokenBlacklisted(String token) {
        return tokenBlacklist.contains(token);
    }
}
