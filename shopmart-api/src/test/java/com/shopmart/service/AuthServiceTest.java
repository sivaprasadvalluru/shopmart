package com.shopmart.service;

import com.shopmart.dto.AuthResponse;
import com.shopmart.dto.LoginRequest;
import com.shopmart.dto.RegisterRequest;
import com.shopmart.exception.EmailAlreadyExistsException;
import com.shopmart.model.entity.User;
import com.shopmart.model.enums.Role;
import com.shopmart.repository.UserRepository;
import com.shopmart.security.JwtTokenProvider;
import com.shopmart.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    private User savedUser;

    @BeforeEach
    void setUp() {
        savedUser = User.builder().id(1L).email("new@shopmart.com").password("encoded-hash").role(Role.CUSTOMER).build();
    }

    @Test
    void register_newEmail_savesUserAndReturnsToken() {
        RegisterRequest request = new RegisterRequest("new@shopmart.com", "plaintext1");
        when(userRepository.existsByEmail("new@shopmart.com")).thenReturn(false);
        when(passwordEncoder.encode("plaintext1")).thenReturn("encoded-hash");
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                UserPrincipal.fromUser(savedUser), null, UserPrincipal.fromUser(savedUser).getAuthorities());
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.email()).isEqualTo("new@shopmart.com");
        assertThat(response.role()).isEqualTo(Role.CUSTOMER);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("new@shopmart.com");
        assertThat(captor.getValue().getPassword()).isEqualTo("encoded-hash");
        assertThat(captor.getValue().getRole()).isEqualTo(Role.CUSTOMER);
    }

    @Test
    void register_existingEmail_throwsEmailAlreadyExistsException() {
        RegisterRequest request = new RegisterRequest("dup@shopmart.com", "plaintext1");
        when(userRepository.existsByEmail("dup@shopmart.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, org.mockito.Mockito.never()).save(any());
    }

    @Test
    void login_validCredentials_returnsToken() {
        LoginRequest request = new LoginRequest("customer@shopmart.com", "customer123");
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                UserPrincipal.fromUser(savedUser), null, UserPrincipal.fromUser(savedUser).getAuthorities());
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(userRepository.findByEmail("customer@shopmart.com")).thenReturn(Optional.of(savedUser));
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.email()).isEqualTo(savedUser.getEmail());
        assertThat(response.role()).isEqualTo(savedUser.getRole());
    }

    @Test
    void login_badCredentials_propagatesException() {
        LoginRequest request = new LoginRequest("customer@shopmart.com", "wrong-password");
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);

        verify(jwtTokenProvider, org.mockito.Mockito.never()).generateToken(any());
    }

    @Test
    void login_authenticatedUserMissingFromDatabase_throwsIllegalStateException() {
        LoginRequest request = new LoginRequest("ghost@shopmart.com", "whatever1");
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                UserPrincipal.fromUser(savedUser), null, UserPrincipal.fromUser(savedUser).getAuthorities());
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(userRepository.findByEmail("ghost@shopmart.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(IllegalStateException.class);
    }
}
