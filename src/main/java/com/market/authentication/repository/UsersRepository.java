package com.market.authentication.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.market.authentication.enums.Role;
import com.market.authentication.model.Users;
import com.market.email.EmailOtp;

public interface UsersRepository extends JpaRepository<Users, Long> {
	
	 // Fetch user by username (for login / authentication)
    Optional<Users> findByUsername(String username);

    // Fetch user by phone number (important if login using phone)
    Optional<Users> findByPhoneNumber(String phoneNumber);

    // Check if username already exists (prevent duplicate registration)
    boolean existsByUsername(String username);

    // Check if phone already exists
    boolean existsByPhoneNumber(String phoneNumber);

    // Get all users by specific role (e.g., ADMIN, VENDOR, CUSTOMER)
    List<Users> findByRole(Role role);
    
    boolean existsByEmail(String email);
    
    @Query("select u from Users u where u.username = ?1OR u.passwordHash= ?1")
    Optional<Users> findByUsernameOrPhoneNumber(String input);

	Optional<Users> findByEmail(String email);





}
