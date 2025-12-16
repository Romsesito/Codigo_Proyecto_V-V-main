package com.yourcompany.artcommision.model;

import org.openxava.annotations.DescriptionsList;
import org.openxava.annotations.Tab;
import org.openxava.annotations.View;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "historial_auditoria")
@View(members="fechaHora, actor, estadoAnterior, estadoNuevo, comision")
@Tab(properties="idHistorial, fechaHora, actor.nombre, estadoAnterior, estadoNuevo")
public class HistorialAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historial")
    private Long idHistorial;

    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaHora;

    private String estadoAnterior;

    private String estadoNuevo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_actor")
    @DescriptionsList(descriptionProperties="nombre, email")
    private Usuario actor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_comision")
    private Comision comision;


    public Long getIdHistorial() { return idHistorial; }
    public void setIdHistorial(Long idHistorial) { this.idHistorial = idHistorial; }

    public Date getFechaHora() { return fechaHora; }
    public void setFechaHora(Date fechaHora) { this.fechaHora = fechaHora; }

    public String getEstadoAnterior() { return estadoAnterior; }
    public void setEstadoAnterior(String estadoAnterior) { this.estadoAnterior = estadoAnterior; }

    public String getEstadoNuevo() { return estadoNuevo; }
    public void setEstadoNuevo(String estadoNuevo) { this.estadoNuevo = estadoNuevo; }

    public Usuario getActor() { return actor; }
    public void setActor(Usuario actor) { this.actor = actor; }

    public Comision getComision() { return comision; }
    public void setComision(Comision comision) { this.comision = comision; }
}