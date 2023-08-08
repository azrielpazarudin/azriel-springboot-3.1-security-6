package com.azriel.services;

import com.azriel.enumerations.TokenType;
import com.azriel.payload.request.AuthenticationRequest;
import com.azriel.payload.request.RegisterRequest;
import com.azriel.payload.response.AuthenticationFailed;
import com.azriel.payload.response.AuthenticationResponse;
import com.azriel.models.User;
import com.azriel.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthenticationService  {

        private final PasswordEncoder passwordEncoder;
        private final JwtService jwtService;
        private final UserRepository userRepository;
        private final AuthenticationManager authenticationManager;
        private final RefreshTokenService refreshTokenService;

        public Object register(RegisterRequest request) {
                if(userRepository.findByEmail(request.getEmail()).isPresent()){
                        System.out.println("MASUK");
                        return AuthenticationFailed.builder().messages(new ArrayList<>(Arrays.asList("Email Sudah Terdaftar")));
                }
                var user = User.builder()
                                .firstName(request.getFirstname())
                                .lastName(request.getLastname())
                                .email(request.getEmail())
                                .password(passwordEncoder.encode(request.getPassword()))
                                .role(request.getRole())
                                .build();
                user = userRepository.save(user);
                var jwt = jwtService.generateToken(user);
                var refreshToken = refreshTokenService.createRefreshToken(user.getId());

                var roles = user.getRole().getAuthority()
                                .stream()
                                .map(SimpleGrantedAuthority::getAuthority)
                                .toList();

                return AuthenticationResponse.builder()
                                .accessToken(jwt)
                                .email(user.getEmail())
                                .id(user.getId())
                                .refreshToken(refreshToken.getToken())
                                .roles(roles)
                                .tokenType(TokenType.BEARER.name())
                                .build();
        }

        public AuthenticationResponse authenticate(AuthenticationRequest request) {
                authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

                var user = userRepository.findByEmail(request.getEmail())
                                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));
                var roles = user.getRole().getAuthority()
                                .stream()
                                .map(SimpleGrantedAuthority::getAuthority)
                                .toList();
                var jwt = jwtService.generateToken(user);
                var refreshToken = refreshTokenService.createRefreshToken(user.getId());
                return AuthenticationResponse.builder()
                                .accessToken(jwt)
                                .roles(roles)
                                .email(user.getEmail())
                                .id(user.getId())
                                .refreshToken(refreshToken.getToken())
                                .tokenType(TokenType.BEARER.name())
                                .build();
        }
}
