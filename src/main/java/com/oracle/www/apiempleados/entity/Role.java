package com.oracle.www.apiempleados.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, unique = true, length = 50)
    private String nombre;

    @ManyToMany(mappedBy = "roles")
    private Set<UsuariosLogin> usuarios = new HashSet<>();

    public Role() {
    }

    public Role(Long id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public Role(String nombre) {
        this.nombre = nombre;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public Set<UsuariosLogin> getUsuarios() {
        return usuarios;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setUsuarios(Set<UsuariosLogin> usuarios) {
        this.usuarios = usuarios;
    }
}