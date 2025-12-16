package com.yourcompany.artcommision.model;

import org.openxava.annotations.DescriptionsList;
import org.openxava.annotations.ListProperties;
import org.openxava.annotations.Tab;
import org.openxava.annotations.View;

import javax.persistence.*;
import javax.validation.constraints.Digits;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "comision")
@View(members=
        "descripcion; monto; fechaCreacion, fechaLimite; estado, motivoCancelacion; " +
                "cliente; artista; entregables")
@Tab(properties="idComision, descripcion, monto, estado, fechaCreacion, fechaLimite")
public class Comision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_comision")
    private Long idComision;

    @Column(length = 1000)
    private String descripcion;

    @Digits(integer=12, fraction=2)
    @Column(precision = 14, scale = 2)
    private BigDecimal monto;

    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCreacion;

    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaLimite;

    @Enumerated(EnumType.STRING)
    private EstadoComision estado;

    @Column(length = 1000)
    private String motivoCancelacion;

    // Relación con cliente (Usuario tipo CLIENTE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente")
    @DescriptionsList(descriptionProperties="nombre, email")
    private Usuario cliente;

    // Relación con artista (Usuario tipo ARTISTA)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_artista")
    @DescriptionsList(descriptionProperties="nombre, especialidad")
    private Usuario artista;

    // entregables
    @OneToMany(mappedBy = "comision", cascade = CascadeType.ALL, orphanRemoval = true)
    @ListProperties("idEntregable, urlArchivo, fechaSubida")
    private List<Entregable> entregables = new ArrayList<>();

    // historial
    @OneToMany(mappedBy = "comision", cascade = CascadeType.ALL, orphanRemoval = true)
    @ListProperties("idHistorial, fechaHora, estadoAnterior, estadoNuevo, actor.nombre")
    private List<HistorialAuditoria> historial = new ArrayList<>();

    // ----- getters / setters -----
    public Long getIdComision() { return idComision; }
    public void setIdComision(Long idComision) { this.idComision = idComision; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public Date getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Date fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public Date getFechaLimite() { return fechaLimite; }
    public void setFechaLimite(Date fechaLimite) { this.fechaLimite = fechaLimite; }

    public EstadoComision getEstado() { return estado; }
    public void setEstado(EstadoComision estado) { this.estado = estado; }

    public String getMotivoCancelacion() { return motivoCancelacion; }
    public void setMotivoCancelacion(String motivoCancelacion) { this.motivoCancelacion = motivoCancelacion; }

    public Usuario getCliente() { return cliente; }
    public void setCliente(Usuario cliente) { this.cliente = cliente; }

    public Usuario getArtista() { return artista; }
    public void setArtista(Usuario artista) { this.artista = artista; }

    public List<Entregable> getEntregables() { return entregables; }
    public void setEntregables(List<Entregable> entregables) { this.entregables = entregables; }

    public List<HistorialAuditoria> getHistorial() { return historial; }
    public void setHistorial(List<HistorialAuditoria> historial) { this.historial = historial; }
    
    
    public String getNivelRiesgo() {
        com.yourcompany.artcommision.calculators.CalculadoraRiesgo calc =
            new com.yourcompany.artcommision.calculators.CalculadoraRiesgo();
        return calc.determinarNivelRiesgo(this);
    }
}