package com.market.email;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.market.authentication.dto.UserRegistrationConfirm;
import com.market.authentication.enums.Role;
import com.market.authentication.model.ResetToken;
import com.market.authentication.model.Users;
import com.market.authentication.repository.ResetTokenRepository;
import com.market.authentication.repository.UsersRepository;
import com.market.security.JwtUtil;

import jakarta.transaction.Transactional;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private EmailOtpRepository emailOtpRepository;
    
    @Autowired
    private UsersRepository usersRepository;
    
    @Autowired
	private PasswordEncoder passwordEncoder;
    
	@Autowired
	private JwtUtil jwtUtil;
	
	@Autowired
	private ResetTokenRepository tokenRepository;
	

    public void sendOtp(String email, String otp) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Account Verification OTP");
        message.setText(
            "Your OTP is: " + otp +
            "\nValid for 5 minutes.\nDo not share it."
        );

        mailSender.send(message);
    }
    
    public ResponseEntity<?> sendSignupOtp(String email) {

        String otp = String.valueOf(100000 + new Random().nextInt(900000));

        EmailOtp emailOtp = emailOtpRepository
                .findByEmail(email)
                .orElse(new EmailOtp());

        emailOtp.setEmail(email);
        emailOtp.setOtp(otp);
        emailOtp.setExpiryTime(LocalDateTime.now().plusMinutes(5));

        emailOtpRepository.save(emailOtp);
        sendOtp(email, otp);

        return ResponseEntity.ok("OTP sent to email");
    }
    @Transactional
    public ResponseEntity<?> verifyOtpAndCreateAccount(UserRegistrationConfirm dto) {

        EmailOtp emailOtp = emailOtpRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("OTP not found"));

        if (emailOtp.getExpiryTime().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("OTP expired");
        }

        if (!emailOtp.getOtp().trim().equals(dto.getOtp().trim())) {

        	System.out.println(emailOtp.getOtp());
        	System.out.println(dto.getOtp());
            return ResponseEntity.badRequest().body("Invalid OTP");
        }

        if (usersRepository.existsByEmail(dto.getEmail())) {
            return ResponseEntity.badRequest().body("Email already registered");
        }

        Users user = new Users();
        user.setUsername(dto.getUsername());
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.USER);

        usersRepository.save(user);
        emailOtpRepository.deleteByEmail(dto.getEmail());

        return ResponseEntity.ok("Account created successfully");
    }

    
    public ResponseEntity<?> sendForgotPasswordOtp(String email) {

        // 1. Check user exists
        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with this email"));

        // 2. Prevent resend within 5 min
        emailOtpRepository.findByEmail(email).ifPresent(existing -> {
            if (existing.getExpiryTime().isAfter(LocalDateTime.now())) {
                throw new RuntimeException("OTP already sent. Please wait 5 minutes.");
            }
        });

        // 3. Generate OTP
        String otp = String.valueOf(100000 + new Random().nextInt(900000));

        // 4. Save OTP
        EmailOtp emailOtp = emailOtpRepository.findByEmail(email)
                .orElse(new EmailOtp());

        emailOtp.setEmail(email);
        emailOtp.setOtp(otp);
        emailOtp.setExpiryTime(LocalDateTime.now().plusMinutes(5));

        emailOtpRepository.save(emailOtp);

        // 5. Send email
        sendOtp(email, otp);

        return ResponseEntity.ok("OTP sent to registered email");
    }

    
    @Transactional
    public ResponseEntity<?> verifyEmailOtpForForgotPassword(String usernameOrPhone, String otp) {
    	
    	Users user=usersRepository.findByUsernameOrPhoneNumber(usernameOrPhone).orElseThrow(() -> new RuntimeException("User not found"));

        EmailOtp emailOtp = emailOtpRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new RuntimeException("OTP not found"));

        if (emailOtp.getExpiryTime().isBefore(LocalDateTime.now())) {
            emailOtpRepository.deleteByEmail(user.getEmail());
            return ResponseEntity.badRequest().body("OTP expired");
        }

        if (!emailOtp.getOtp().trim().equals(otp.trim())) {
            return ResponseEntity.badRequest().body("Invalid OTP");
        }

       

        // Generate reset password JWT
        String resetToken = jwtUtil.generateResetPasswordToken(user.getUsername());
        
        Optional<ResetToken> existingToken = tokenRepository.findByUser(user);
        
        ResetToken rst;
        if (existingToken.isPresent()) {
            // 🔄 Update existing token
            rst = existingToken.get();
            rst.setJwtToken("Bearer " + resetToken);
            rst.setCreatedTime(LocalDateTime.now());
        } else {
            // 🆕 Create new token
            rst = new ResetToken();
            rst.setJwtToken("Bearer " + resetToken);
            rst.setCreatedTime(LocalDateTime.now());
            rst.setUser(user);
        }

        // ✅ Missing line — save the token
        tokenRepository.save(rst);

        emailOtpRepository.deleteByEmail(user.getEmail());

        return ResponseEntity.ok(Map.of(
                "token", "Bearer " + resetToken,
                "username", user.getUsername()
        ));
    }


}
