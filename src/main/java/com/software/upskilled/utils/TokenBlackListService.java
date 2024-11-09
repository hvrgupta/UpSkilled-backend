package com.software.upskilled.utils;

import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

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
