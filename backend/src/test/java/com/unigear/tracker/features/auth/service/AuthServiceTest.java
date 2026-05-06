package com.unigear.tracker.features.auth.service;

import com.unigear.tracker.features.auth.dto.AuthResponse;
import com.unigear.tracker.features.auth.dto.LoginRequest;
import com.unigear.tracker.features.auth.dto.RegisterRequest;
import com.unigear.tracker.features.user.entity.User;
import com.unigear.tracker.features.user.repository.UserRepository;
import com.unigear.tracker.features.auth.security.JwtUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerCreatesUserWhenEmailIsNew() {
        ReflectionTestUtils.setField(authService, "adminEmail", "admin@unigear.com");
        RegisterRequest request = new RegisterRequest("Test User", "test@unigear.com", "password123");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        AuthResponse response = authService.register(request);

        Assertions.assertEquals(1L, response.getId());
        Assertions.assertEquals("test@unigear.com", response.getEmail());
        Assertions.assertEquals("Registration successful", response.getMessage());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User savedUser = captor.getValue();
        Assertions.assertNotEquals("password123", savedUser.getPassword());
        Assertions.assertTrue(new BCryptPasswordEncoder().matches("password123", savedUser.getPassword()));
    }

    @Test
    void registerThrowsWhenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest("Test User", "test@unigear.com", "password123");
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        RuntimeException ex = Assertions.assertThrows(RuntimeException.class, () -> authService.register(request));
        Assertions.assertEquals("Email already registered", ex.getMessage());
    }

    @Test
    void loginReturnsTokenForValidCredentials() {
        String rawPassword = "password123";
        String encodedPassword = new BCryptPasswordEncoder().encode(rawPassword);
        User user = new User("Test User", "test@unigear.com", encodedPassword);
        user.setId(1L);
        user.setRole(User.Role.STUDENT);

        when(userRepository.findByEmail("test@unigear.com")).thenReturn(Optional.of(user));
        when(jwtUtil.generateJwtToken("test@unigear.com")).thenReturn("jwt-token");

        AuthResponse response = authService.login(new LoginRequest("test@unigear.com", rawPassword));

        Assertions.assertEquals("jwt-token", response.getAccessToken());
        Assertions.assertEquals("STUDENT", response.getRole());
    }

    @Test
    void loginThrowsForInvalidPassword() {
        User user = new User("Test User", "test@unigear.com", new BCryptPasswordEncoder().encode("correct"));
        when(userRepository.findByEmail("test@unigear.com")).thenReturn(Optional.of(user));

        RuntimeException ex = Assertions.assertThrows(RuntimeException.class,
                () -> authService.login(new LoginRequest("test@unigear.com", "wrong")));

        Assertions.assertEquals("Invalid email or password", ex.getMessage());
    }

    @Test
    void oauth2AuthenticationCreatesUserWhenNotExisting() {
        OAuth2User oAuth2User = org.mockito.Mockito.mock(OAuth2User.class);
        when(oAuth2User.getAttribute("email")).thenReturn("oauth@unigear.com");
        when(oAuth2User.getAttribute("name")).thenReturn("OAuth User");
        when(oAuth2User.getAttribute("picture")).thenReturn("https://img.example/pic.png");

        when(userRepository.findByEmail("oauth@unigear.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(2L);
            return user;
        });
        when(jwtUtil.generateJwtToken(anyString())).thenReturn("oauth-token");

        AuthResponse response = authService.authenticateWithGoogleOAuth2User(oAuth2User);

        Assertions.assertEquals(2L, response.getId());
        Assertions.assertEquals("oauth-token", response.getAccessToken());
        verify(userRepository).save(any(User.class));
    }
}
