package com.omnify.blogapp.service;

import com.omnify.blogapp.dto.JwtResponse;
import com.omnify.blogapp.exception.EmailAlreadyExistsException;
import com.omnify.blogapp.exception.InvalidCredentialsException;
import com.omnify.blogapp.model.User;
import com.omnify.blogapp.payload.LoginRequest;
import com.omnify.blogapp.payload.SignupRequest;
import com.omnify.blogapp.repository.UserRepository;
import com.omnify.blogapp.security.JwtUtils;
import com.omnify.blogapp.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;

    @Autowired
    public AuthService(AuthenticationManager authenticationManager,
                      UserRepository userRepository,
                      PasswordEncoder encoder,
                      JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.encoder = encoder;
        this.jwtUtils = jwtUtils;
    }

    /**
     * Authenticates a user and returns JWT token
     * @param loginRequest containing email and password
     * @return JwtResponse with token and user details
     * @throws InvalidCredentialsException if authentication fails
     */
    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            String jwt = jwtUtils.generateJwtToken(userDetails.getUsername());

            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            return new JwtResponse(
                    jwt,
                    userDetails.getId(),
                    userDetails.getUsername(),
                    userDetails.getName(),
                    userDetails.getEmail(),
                    roles
            );
        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException("Invalid email or password");
        }
    }

    /**
     * Registers a new user
     * @param signUpRequest containing name, email and password
     * @throws EmailAlreadyExistsException if email is already registered
     */
    public void registerUser(SignupRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new EmailAlreadyExistsException("Email is already in use!");
        }

        User user = new User();
        user.setName(signUpRequest.getName());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(encoder.encode(signUpRequest.getPassword()));

        userRepository.save(user);
    }

    /**
     * Gets the current authenticated user
     * @return UserDetails of the current user
     * @throws IllegalStateException if no user is authenticated
     */
    public UserDetailsImpl getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }
        
        return (UserDetailsImpl) authentication.getPrincipal();
    }
}