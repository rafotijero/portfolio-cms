package dev.rafotijero.cms.security;

import java.time.Instant;

public record IssuedToken(String token, Instant expiresAt) {
}
