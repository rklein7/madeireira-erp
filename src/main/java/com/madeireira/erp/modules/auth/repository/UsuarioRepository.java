package com.madeireira.erp.modules.auth.repository;

import com.madeireira.erp.modules.auth.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
    Page<Usuario> findByPerfil(String perfil, Pageable pageable);
}
