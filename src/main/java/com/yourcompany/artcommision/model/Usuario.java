package com.yourcompany.artcommision.model;

import org.openxava.annotations.Required;
import org.openxava.annotations.Stereotype;
import org.openxava.annotations.Tab;
import org.openxava.annotations.View;

import javax.persistence.*;

@Entity
@Table(name = "usuario")
@View(members="nombre, email; password; tipoUsuario; especialidad")
@Tab(properties="idUsuario, nombre, email, tipoUsuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id_usuario")
    private Long idUsuario;

    @Required
    @Column(nullable = false)
    private String nombre;

    @Required
    @Column(nullable = false, unique = true)
    private String email;

    @Stereotype("PASSWORD")
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name="tipo_usuario", nullable = false)
    private TipoUsuario tipoUsuario;


    private String especialidad;


    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public TipoUsuario getTipoUsuario() { return tipoUsuario; }
    public void setTipoUsuario(TipoUsuario tipoUsuario) { this.tipoUsuario = tipoUsuario; }

    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }
}