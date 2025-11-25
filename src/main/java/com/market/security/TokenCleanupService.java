package com.market.security;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.market.authentication.model.ResetToken;
import com.market.authentication.repository.ResetTokenRepository;

import jakarta.transaction.Transactional;

@Service
public class TokenCleanupService {

    @Autowired
    private ResetTokenRepository tokenRepository;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void deleteOldTokens() {
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
        tokenRepository.deleteTokensOlderThan(fiveMinutesAgo);
        System.out.println("🧹 Deleted tokens older than 5 minutes at: " + LocalDateTime.now());
    }
}

