package com.oracle.www.apiempleados;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class ApiEmpleadosApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiEmpleadosApplication.class, args);
        // contraseña segura
        System.out.println(new BCryptPasswordEncoder().encode("12345"));
    }

}
