package com.oracle.www.apiempleados.security.repository;

import com.oracle.www.apiempleados.entity.UsuariosLogin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuariosLoginRepository extends JpaRepository<UsuariosLogin, Long> {

    //UsuariosLogin findByUsername (String username);

    Optional<UsuariosLogin> findByUsername(String username);
}
