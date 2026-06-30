package com.madeireira.erp.modules.auth.service;

import com.madeireira.erp.modules.auth.dto.AuthDTO;
import com.madeireira.erp.modules.auth.entity.Usuario;
import com.madeireira.erp.modules.auth.repository.UsuarioRepository;
import com.madeireira.erp.shared.exception.BusinessException;
import com.madeireira.erp.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public AuthDTO.LoginResponse login(AuthDTO.LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getSenha()));

        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        return AuthDTO.LoginResponse.builder()
                .token(jwtService.generateToken(usuario))
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .perfil(usuario.getPerfil())
                .build();
    }

    @Transactional
    public AuthDTO.LoginResponse registrar(AuthDTO.RegisterRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("E-mail já cadastrado: " + request.getEmail());
        }

        Usuario usuario = Usuario.builder()
                .nome(request.getNome())
                .email(request.getEmail())
                .senha(passwordEncoder.encode(request.getSenha()))
                .perfil(request.getPerfil() != null ? request.getPerfil() : "OPERADOR")
                .build();

        usuario = usuarioRepository.save(usuario);

        return AuthDTO.LoginResponse.builder()
                .token(jwtService.generateToken(usuario))
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .perfil(usuario.getPerfil())
                .build();
    }
}
