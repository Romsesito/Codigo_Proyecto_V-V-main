package com.yourcompany.artcommision.services;

import com.yourcompany.artcommision.exceptions.*;
import com.yourcompany.artcommision.model.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ComisionService {

    private Map<Long, Comision> comisiones = new ConcurrentHashMap<>();
    private Map<Long, Object> bloqueos = new ConcurrentHashMap<>();
    private long secuencia = 1L;
    private boolean conexionActiva = true;

    public void setConexionActiva(boolean activa) {
        this.conexionActiva = activa;
    }

    public Comision registrarComision(String descripcion, BigDecimal monto, Date fechaLimite,
                                       Usuario cliente, Usuario artista) {
        if (!conexionActiva) {
            throw new DatabaseConnectionException("Intente nuevamente más tarde");
        }

        List<String> camposVacios = new ArrayList<>();
        if (descripcion == null || descripcion.trim().isEmpty()) {
            camposVacios.add("descripcion");
        }
        if (monto == null) {
            camposVacios.add("monto");
        }
        if (cliente == null) {
            camposVacios.add("cliente");
        }

        if (!camposVacios.isEmpty()) {
            throw new CamposObligatoriosException("Campos obligatorios vacíos: " + camposVacios);
        }

        Comision comision = new Comision();
        comision.setIdComision(secuencia++);
        comision.setDescripcion(descripcion);
        comision.setMonto(monto);
        comision.setFechaCreacion(new Date());
        comision.setFechaLimite(fechaLimite);
        comision.setEstado(EstadoComision.NUEVA);
        comision.setCliente(cliente);
        comision.setArtista(artista);

        comisiones.put(comision.getIdComision(), comision);
        return comision;
    }

    public boolean validarParticipante(Usuario usuario) {
        if (usuario == null) return false;
        if (usuario.getNombre() == null || usuario.getNombre().trim().isEmpty()) return false;
        if (usuario.getEmail() == null || usuario.getEmail().trim().isEmpty()) return false;
        if (usuario.getTipoUsuario() == null) return false;
        return true;
    }

    public Comision actualizarEstado(Long idComision, EstadoComision nuevoEstado, Usuario actor) {
        Comision comision = comisiones.get(idComision);
        if (comision == null) {
            throw new ComisionNotFoundException("Comisión no encontrada: " + idComision);
        }

        if (esEstadoTerminal(comision.getEstado())) {
            throw new EstadoTerminalException("No se puede modificar una comisión en estado terminal: " + comision.getEstado());
        }

        if (!esTransicionValida(comision.getEstado(), nuevoEstado)) {
            throw new TransicionInvalidaException("Transición no permitida de " + comision.getEstado() + " a " + nuevoEstado);
        }

        HistorialAuditoria historial = new HistorialAuditoria();
        historial.setFechaHora(new Date());
        historial.setEstadoAnterior(comision.getEstado().name());
        historial.setEstadoNuevo(nuevoEstado.name());
        historial.setActor(actor);
        historial.setComision(comision);
        comision.getHistorial().add(historial);

        comision.setEstado(nuevoEstado);
        return comision;
    }

    private boolean esEstadoTerminal(EstadoComision estado) {
        return estado == EstadoComision.CANCELADA || estado == EstadoComision.COMPLETADA;
    }

    private boolean esTransicionValida(EstadoComision actual, EstadoComision nuevo) {
        switch (actual) {
            case NUEVA:
                return nuevo == EstadoComision.EN_PROGRESO || nuevo == EstadoComision.CANCELADA;
            case EN_PROGRESO:
                return nuevo == EstadoComision.EN_REVISION || nuevo == EstadoComision.CANCELADA;
            case EN_REVISION:
                return nuevo == EstadoComision.COMPLETADA || nuevo == EstadoComision.EN_PROGRESO || nuevo == EstadoComision.CANCELADA;
            default:
                return false;
        }
    }

    public boolean puedeAdjuntarEntregables(Comision comision) {
        return comision.getEstado() == EstadoComision.EN_REVISION;
    }

    public Comision aprobarComision(Long idComision, Usuario actor) {
        Comision comision = comisiones.get(idComision);
        if (comision == null) {
            throw new ComisionNotFoundException("Comisión no encontrada");
        }

        if (comision.getEstado() != EstadoComision.NUEVA) {
            throw new AccionNoPermitidaException("Solo se pueden aprobar comisiones en estado NUEVA");
        }

        return actualizarEstado(idComision, EstadoComision.EN_PROGRESO, actor);
    }

    public synchronized Comision cancelarComision(Long idComision, String motivo, Usuario actor) {
        if (bloqueos.containsKey(idComision)) {
            throw new ConcurrenciaException("Comisión bloqueada por otro usuario");
        }

        try {
            bloqueos.put(idComision, new Object());

            Comision comision = comisiones.get(idComision);
            if (comision == null) {
                throw new ComisionNotFoundException("Comisión no encontrada");
            }

            if (esEstadoTerminal(comision.getEstado())) {
                throw new EstadoTerminalException("No se puede cancelar una comisión en estado terminal");
            }

            if (motivo == null || motivo.trim().isEmpty()) {
                throw new MotivoRequeridoException("El motivo de cancelación es requerido");
            }

            comision.setMotivoCancelacion(motivo);
            
            HistorialAuditoria historial = new HistorialAuditoria();
            historial.setFechaHora(new Date());
            historial.setEstadoAnterior(comision.getEstado().name());
            historial.setEstadoNuevo(EstadoComision.CANCELADA.name());
            historial.setActor(actor);
            historial.setComision(comision);
            comision.getHistorial().add(historial);

            comision.setEstado(EstadoComision.CANCELADA);
            return comision;
        } finally {
            bloqueos.remove(idComision);
        }
    }

    public Comision obtenerComision(Long idComision) {
        return comisiones.get(idComision);
    }

    public List<Comision> consultarComisiones(Usuario usuario) {
        if (usuario == null) return new ArrayList<>();
        
        return comisiones.values().stream()
                .filter(c -> perteneceAUsuario(c, usuario))
                .collect(Collectors.toList());
    }

    public List<Comision> filtrarPorEstado(Usuario usuario, EstadoComision estado) {
        return consultarComisiones(usuario).stream()
                .filter(c -> c.getEstado() == estado)
                .collect(Collectors.toList());
    }

    private boolean perteneceAUsuario(Comision comision, Usuario usuario) {
        if (usuario.getTipoUsuario() == TipoUsuario.ADMIN) {
            return true;
        }
        if (usuario.getTipoUsuario() == TipoUsuario.CLIENTE) {
            return comision.getCliente() != null && 
                   comision.getCliente().getIdUsuario().equals(usuario.getIdUsuario());
        }
        if (usuario.getTipoUsuario() == TipoUsuario.ARTISTA) {
            return comision.getArtista() != null && 
                   comision.getArtista().getIdUsuario().equals(usuario.getIdUsuario());
        }
        return false;
    }

    public List<HistorialAuditoria> obtenerHistorial(Long idComision) {
        Comision comision = comisiones.get(idComision);
        if (comision == null) {
            throw new ComisionNotFoundException("Comisión no encontrada");
        }
        return comision.getHistorial();
    }

    public void agregarComision(Comision comision) {
        if (comision.getIdComision() == null) {
            comision.setIdComision(secuencia++);
        }
        comisiones.put(comision.getIdComision(), comision);
    }

    public void limpiar() {
        comisiones.clear();
        bloqueos.clear();
        secuencia = 1L;
    }
}
