
package com.pasteleriamilsabores.pasteleria.api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "boleta")
public class Boleta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime fecha;

    @NotNull
    @Min(0)
    @Column(nullable = false)
    private Integer subtotal;

    @NotNull
    @Min(0)
    @Column(nullable = false)
    private Integer descuento;

    @NotNull
    @Min(0)
    @Column(nullable = false)
    private Integer total;

    @Column(name = "cupon")
    private String cupon;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @OneToMany(mappedBy = "boleta", cascade = CascadeType.ALL)
    private List<DetalleBoleta> detalles = new ArrayList<>();

    public Boleta() {
    }

    @PrePersist
    public void asignarFecha() {
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public Integer getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Integer subtotal) {
        this.subtotal = subtotal;
    }

    public Integer getDescuento() {
        return descuento;
    }

    public void setDescuento(Integer descuento) {
        this.descuento = descuento;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public String getCupon() {
        return cupon;
    }

    public void setCupon(String cupon) {
        this.cupon = cupon;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public List<DetalleBoleta> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleBoleta> detalles) {
        this.detalles = detalles;
    }
}
