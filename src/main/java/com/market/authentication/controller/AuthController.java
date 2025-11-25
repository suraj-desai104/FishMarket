package com.market.authentication.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.market.authentication.dto.ForgotPasswordRequestDTO;
import com.market.authentication.dto.LoginRequest;
import com.market.authentication.dto.ResetPasswordDTO;
import com.market.authentication.dto.UserRegistrationConfirm;
import com.market.authentication.dto.UserRegistrationRequest;
import com.market.authentication.dto.VerifyOtpRequestDTO;
import com.market.authentication.model.Users;
import com.market.authentication.repository.UsersRepository;
import com.market.authentication.services.AuthService;

@RestController
@RequestMapping("/api/auth")   // Common base path for authentication
@CrossOrigin(origins = "*") 
public class AuthController {

    @Autowired
    private AuthService authService;
    
    @Autowired
    private UsersRepository usersRepository;

    // User Registration Endpoint
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserRegistrationRequest request) {
        return authService.sendOtp(request.getPhoneNumber());
    }
    
    @PostMapping("/register-confirm")
    public ResponseEntity<?> registerUserConfirm(@RequestBody UserRegistrationConfirm request) {
        return authService.registerUser(request);
    }

    // Login Endpoint
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
   
        return authService.login(request);
    }
    
  
    @PostMapping("/forgot")
    public ResponseEntity<?> forgetPass(@RequestBody ForgotPasswordRequestDTO forgotPasswordRequestDTO){
    	
    	Users user=usersRepository.findByUsernameOrPhoneNumber(forgotPasswordRequestDTO.getUsernameOrPhone()).orElseThrow();
    	    	 
    	 return authService.sendOtp(user.getPhoneNumber());  	
    }
    
    @PostMapping("/Verify-forget-otp")
    public ResponseEntity<?> VerifyForgetOtp(@RequestBody VerifyOtpRequestDTO otpRequestDTO){
    	return authService.forgetotpVerifyPassword(otpRequestDTO);   	
    }
    
    @PostMapping("/reset-pass")
    public ResponseEntity<?> resetPass(@RequestBody ResetPasswordDTO passwordDTO,@RequestHeader("Authorization")  String token){
    	System.out.println(token);
    	return authService.resetPassword(passwordDTO, token);
    }
    
    
    
    
    
    
    
    
    
    
}
