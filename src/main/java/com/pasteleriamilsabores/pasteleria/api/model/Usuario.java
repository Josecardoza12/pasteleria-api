package com.pasteleriamilsabores.pasteleria.api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Usuario del sistema (administrador, vendedor o cliente).
 * Hibernate crea la tabla "usuario" a partir de esta clase.
 */
@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // RUN sin puntos ni guion, con dígito verificador (ej: 19011022K)
    @NotBlank
    @Size(min = 7, max = 9)
    @Column(nullable = false, unique = true, length = 9)
    private String run;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String nombre;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String apellidos;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, unique = true, length = 100)
    private String correo;

    // Se guarda cifrada con BCrypt (siempre empieza con $2a$). Nunca se devuelve en un JSON.
    @NotBlank
    @Column(nullable = false)
    private String contrasena;

    // Opcional
    private LocalDate fechaNacimiento;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String region;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String comuna;

    @NotBlank
    @Size(max = 300)
    @Column(nullable = false, length = 300)
    private String direccion;

    // Se guarda como texto: "ADMINISTRADOR", "VENDEDOR" o "CLIENTE"
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    // JPA necesita el constructor vacío
    public Usuario() {
    }

    public Usuario(String run, String nombre, String apellidos, String correo, String contrasena,
                   LocalDate fechaNacimiento, String region, String comuna, String direccion, Rol rol) {
        this.run = run;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.correo = correo;
        this.contrasena = contrasena;
        this.fechaNacimiento = fechaNacimiento;
        this.region = region;
        this.comuna = comuna;
        this.direccion = direccion;
        this.rol = rol;
    }

    public Long getId() { return id; }

    public String getRun() { return run; }
    public void setRun(String run) { this.run = run; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getComuna() { return comuna; }
    public void setComuna(String comuna) { this.comuna = comuna; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }
}