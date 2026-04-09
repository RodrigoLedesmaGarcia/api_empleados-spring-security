package com.oracle.www.apiempleados.security.repository;

import com.oracle.www.apiempleados.entity.UsuariosLogin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuariosLoginRepository extends JpaRepository<UsuariosLogin, Long> {

    UsuariosLogin findByUsername (String username);
}
