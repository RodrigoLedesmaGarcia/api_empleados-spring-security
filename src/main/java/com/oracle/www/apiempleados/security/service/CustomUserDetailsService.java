package com.oracle.www.apiempleados.security.service;

import com.oracle.www.apiempleados.entity.Role;
import com.oracle.www.apiempleados.entity.UsuariosLogin;
import com.oracle.www.apiempleados.security.repository.UsuariosLoginRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuariosLoginRepository usuariosLoginRepository;

    public CustomUserDetailsService(UsuariosLoginRepository usuariosLoginRepository) {
        this.usuariosLoginRepository = usuariosLoginRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UsuariosLogin usuario = usuariosLoginRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        return User.builder()
                .username(usuario.getUsername())
                .password(usuario.getPassword())
                .authorities(mapRolesToAuthorities(usuario.getRoles()))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }

    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(Collection<Role> roles) {
        return roles.stream()
                .map(Role::getNombre)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }
}