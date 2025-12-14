package com.market.authentication.services;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.market.address.Address;
import com.market.address.AddressRepository;
import com.market.address.AssignAddressDto;
import com.market.authentication.dto.ForgotPasswordRequestDTO;
import com.market.authentication.dto.LoginRequest;
import com.market.authentication.dto.ResetPasswordDTO;
import com.market.authentication.dto.SetDeliveryPinRequest;
import com.market.authentication.dto.UserRegistrationConfirm;
import com.market.authentication.dto.UserRegistrationRequest;
import com.market.authentication.dto.VerifyOtpRequestDTO;
import com.market.authentication.enums.Role;
import com.market.authentication.model.ResetToken;
import com.market.authentication.model.Users;
import com.market.authentication.repository.ResetTokenRepository;
import com.market.authentication.repository.UsersRepository;
import com.market.security.JwtUtil;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;

import jakarta.transaction.Transactional;

@Service
public class AuthService {
	
	
	@Autowired
    private UsersRepository usersRepository;
	
	@Autowired
	private ResetTokenRepository tokenRepository;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
    private  AddressRepository addressRepository;

	@Autowired
	private JwtUtil jwtUtil;
	
	 @Value("${twilio.service-sid}")
	    private String twilioServiceSid;

	public ResponseEntity<?> registerUser(UserRegistrationConfirm request) {
	    if (usersRepository.existsByUsername(request.getUsername())) {
	        return ResponseEntity.badRequest().body("Username already exists!");
	    }
	    if (usersRepository.existsByPhoneNumber(request.getPhoneNumber())) {
	        return ResponseEntity.badRequest().body("Phone number already exists!");
	    }
	   

	    try {
	    VerificationCheck verificationCheck = VerificationCheck.creator(twilioServiceSid)
                .setTo("+91"+request.getPhoneNumber())
                .setCode(request.getOtp())
                .create();
	    
	    
	    if ("approved".equalsIgnoreCase(verificationCheck.getStatus())) {
	    Users user = new Users();
	    user.setUsername(request.getUsername());
	    user.setFullName(request.getFullName());
	    user.setPhoneNumber(request.getPhoneNumber());
	    user.setEmail(request.getEmail());

	    // ✅ Encode password before saving
	    user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

	    // Default role
	    user.setRole(Role.USER);

	    usersRepository.save(user);
	    return ResponseEntity.ok("User registered successfully");
	    }else {
	    	 return ResponseEntity.ok("Invalid or expired OTP.");
	    }
	    }catch (Exception e) {
	    	 return ResponseEntity.ok("OTP verification failed: " + e.getMessage());
		}
	}

	public ResponseEntity<?> login(LoginRequest request) {
	    Users user = usersRepository.findByUsernameOrPhoneNumber(request.getUsernameOrPhone())
	            .orElseThrow(() -> new RuntimeException("User not found"));

	    // ✅ Validate encoded password
//	    System.out.println(request.getUsernameOrPhone());
//	    System.out.println(user.getPasswordHash());
//	    System.out.println(passwordEncoder.matches(request.getPassword(), user.getPasswordHash()));
	    if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
	        throw new RuntimeException("Invalid credentials");
	    }

//	    System.out.println(user.getRole().name());
	    // ✅ Generate JWT Token with role
	    String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());


	    Map<String, Object> response = new HashMap();
	    response.put("token", token);
	    response.put("role", user.getRole().name());
	    response.put("username", user.getUsername());
	    response.put("userId", user.getUserId());

	    return ResponseEntity.ok(response);
	}
	

	public ResponseEntity<String> sendOtp(String phoneNumber) {
		Verification verification = Verification.creator(
                twilioServiceSid,   
              "+91"+phoneNumber, 
                "sms"              
        ).create();


        if ("pending".equalsIgnoreCase(verification.getStatus())) {
            return ResponseEntity.ok("OTP sent successfully to " + phoneNumber);
        } else {
            return ResponseEntity.ok("Failed to send OTP. Status:  " + verification.getStatus());
        }
		

	}
	
	public ResponseEntity<?> forgetotpVerifyPassword(VerifyOtpRequestDTO otpRequestDTO){
		
		Users user=usersRepository.findByUsernameOrPhoneNumber(otpRequestDTO.getUsernameOrPhone()).orElseThrow();
		try {
		 VerificationCheck verificationCheck = VerificationCheck.creator(twilioServiceSid)
	                .setTo("+91"+user.getPhoneNumber())
	                .setCode(otpRequestDTO.getOtp())
	                .create();
		 if ("approved".equalsIgnoreCase(verificationCheck.getStatus())) {
	            // ✅ Generate special reset password JWT token
	            String resetToken = jwtUtil.generateResetPasswordToken(user.getUsername());
	            // ✅ Check if token already exists for user
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
	            Map<String, String> response = new HashMap<>();
	            response.put("token", "Bearer " + resetToken);
	            response.put("username", user.getUsername());
	            return ResponseEntity.ok(response);

	        } else {
	            return ResponseEntity.badRequest().body("OTP is incorrect or expired!");
	        }
	    } catch (Exception e) {
	        return ResponseEntity.badRequest().body("OTP verification failed: " + e.getMessage());
	    }
		 
		 
		
	}
	
	@Transactional
	public ResponseEntity<?> resetPassword(ResetPasswordDTO dto, String token) {

		System.out.println(token);
	    ResetToken rs = tokenRepository.findByJwtToken(token).orElseThrow();
	    if (rs != null) {
	        Users user = usersRepository.findById(rs.getUser().getUserId()).orElseThrow();

	        // ✅ Update password
	        user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));

	        // ✅ Break relationship before delete (important)
	   
	        usersRepository.save(user);

	        // ✅ Now delete token safely
//	        System.out.println("Will delete token id=" + rs.getId() + ", exists? " + tokenRepository.existsById(rs.getId()));
	        tokenRepository.deleteToken(token);
	        tokenRepository.flush(); // ensures immediate DELETE query
//	        System.out.println("After delete, exists? " + tokenRepository.existsById(rs.getId()));

	        return ResponseEntity.ok(Map.of(
	            "message", "Password changed successfully",
	            "username", user.getUsername()
	        ));
	    } else {
	        return ResponseEntity.ok("Reset Password failed, try again!");
	    }
	}

	@Transactional
    public Users assignDefaultAddress(AssignAddressDto dto) {
        Users user = usersRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Address address = addressRepository.findById(dto.getAddressId())
                .orElseThrow(() -> new RuntimeException("Address not found"));

        // Set as default address
        user.setDefaultAddress(address);

        return usersRepository.save(user);
    }
	
	 public ResponseEntity<?> setDeliveryPin(SetDeliveryPinRequest dto) {

	        // STEP 1: Find user
	        Users user = usersRepository.findByUsername(dto.getUsername())
	                .orElseThrow(() -> new RuntimeException("User not found"));

	        // STEP 2: Encode PIN (CRITICAL)
	        String encodedPin = passwordEncoder.encode(dto.getPin());

	        // STEP 3: Save encoded PIN
	        user.setDeliveryPinHash(encodedPin);
	        usersRepository.save(user);

	        // STEP 4: Response
	        return ResponseEntity.ok(
	                Map.of(
	                        "message", "Delivery PIN set successfully",
	                        "username", user.getUsername()
	                )
	        );
	 }
}
