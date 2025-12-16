package com.yourcompany.artcommision.model;

import org.openxava.annotations.Tab;
import org.openxava.annotations.View;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "entregable")
@View(members="urlArchivo; fechaSubida; comision")
@Tab(properties="idEntregable, urlArchivo, fechaSubida, comision.descripcion")
public class Entregable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_entregable")
    private Long idEntregable;

    @Column(name = "url_archivo", length = 1000)
    private String urlArchivo;

    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaSubida;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_comision")
    private Comision comision;


    public Long getIdEntregable() { return idEntregable; }
    public void setIdEntregable(Long idEntregable) { this.idEntregable = idEntregable; }

    public String getUrlArchivo() { return urlArchivo; }
    public void setUrlArchivo(String urlArchivo) { this.urlArchivo = urlArchivo; }

    public Date getFechaSubida() { return fechaSubida; }
    public void setFechaSubida(Date fechaSubida) { this.fechaSubida = fechaSubida; }

    public Comision getComision() { return comision; }
    public void setComision(Comision comision) { this.comision = comision; }
}