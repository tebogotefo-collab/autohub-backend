package com.mathotech.autopartshub.service;

import com.mathotech.autopartshub.dto.auth.AuthRequest;
import com.mathotech.autopartshub.dto.auth.AuthResponse;
import com.mathotech.autopartshub.dto.auth.RegisterRequest;
import com.mathotech.autopartshub.exception.EmailAlreadyExistsException;
import com.mathotech.autopartshub.model.Role;
import com.mathotech.autopartshub.model.Seller;
import com.mathotech.autopartshub.model.User;
import com.mathotech.autopartshub.repository.SellerRepository;
import com.mathotech.autopartshub.repository.UserRepository;
import com.mathotech.autopartshub.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final SellerRepository sellerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already in use");
        }

        // Create user entity
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .role(request.getRole())
                .verified(false) // New users are not verified by default
                .enabled(true)
                .build();
        
        User savedUser = userRepository.save(user);
        
        // If the role is SELLER, create a Seller profile
        if (request.getRole() == Role.ROLE_SELLER && request.getSellerInfo() != null) {
            Seller seller = Seller.builder()
                    .id(savedUser.getId()) // Same ID as user
                    .user(savedUser)
                    .businessName(request.getSellerInfo().getBusinessName())
                    .description(request.getSellerInfo().getDescription())
                    .verificationStatus(Seller.SellerVerificationStatus.UNVERIFIED)
                    .build();
            
            sellerRepository.save(seller);
        }
        
        // Generate tokens
        String jwtToken = jwtUtils.generateToken(user);
        String refreshToken = jwtUtils.generateRefreshToken(user);
        
        return buildAuthResponse(user, jwtToken, refreshToken);
    }

    public AuthResponse authenticate(AuthRequest request) {
        // Authenticate the user
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        
        // If we reach here, authentication was successful
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();
        
        String jwtToken = jwtUtils.generateToken(user);
        String refreshToken = jwtUtils.generateRefreshToken(user);
        
        return buildAuthResponse(user, jwtToken, refreshToken);
    }
    
    private AuthResponse buildAuthResponse(User user, String jwtToken, String refreshToken) {
        return AuthResponse.builder()
                .token(jwtToken)
                .refreshToken(refreshToken)
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .build();
    }
}
