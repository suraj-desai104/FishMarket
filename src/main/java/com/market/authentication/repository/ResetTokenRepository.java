package com.market.authentication.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.market.authentication.model.ResetToken;
import com.market.authentication.model.Users;

import jakarta.transaction.Transactional;

@Repository
public interface ResetTokenRepository extends JpaRepository<ResetToken, Long>{
	
	
	  // 🔍 1️⃣ Find reset token by token string
    Optional<ResetToken> findByJwtToken(String jwtToken);

    // 🔍 2️⃣ Find reset token by user
    Optional<ResetToken> findByUser(Users user);

    // 🔍 3️⃣ Delete reset token by user (useful after password reset)
    void deleteByUser(Users user);

    // 🔍 Get all tokens created before the given time
    List<ResetToken> findByCreatedTimeBefore(LocalDateTime time);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM ResetToken t WHERE t.createdTime < :time")
    void deleteTokensOlderThan(@Param("time") LocalDateTime time);
    
    @Modifying
    @Query("DELETE FROM Users u WHERE u.userId = :id")
    void deleteUserById(@Param("id") Long id);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM ResetToken t WHERE t.jwtToken = ?1")
    void deleteToken(String token);


}
