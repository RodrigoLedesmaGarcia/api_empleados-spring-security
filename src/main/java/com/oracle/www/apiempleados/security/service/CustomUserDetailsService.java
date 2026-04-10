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

import java.util.Collection;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuariosLoginRepository usuariosLoginRepository;

    public CustomUserDetailsService(UsuariosLoginRepository usuariosLoginRepository) {
        this.usuariosLoginRepository = usuariosLoginRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UsuariosLogin usuario = usuariosLoginRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        return new User(
                usuario.getUsername(),
                usuario.getPassword(),
                mapRolesToAuthorities(usuario.getRoles())
        );
    }

    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(Collection<Role> roles) {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getNombre()))
                .collect(Collectors.toSet());
    }
}