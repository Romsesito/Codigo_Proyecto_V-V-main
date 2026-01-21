package com.yourcompany.artcommision.tests;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.*;

import com.yourcompany.artcommision.exceptions.*;
import com.yourcompany.artcommision.model.*;
import com.yourcompany.artcommision.services.ComisionService;

/**
 * CU-02: Actualizar estado de comisión
 * Casos de prueba para la actualización de estados
 */
public class CU02_ActualizarEstadoComisionTest {

    private ComisionService service;
    private Usuario cliente;
    private Usuario artista;
    private Usuario admin;

    @Before
    public void setUp() {
        service = new ComisionService();
        
        cliente = new Usuario();
        cliente.setIdUsuario(1L);
        cliente.setNombre("Cliente Test");
        cliente.setEmail("cliente@test.com");
        cliente.setPassword("password123");
        cliente.setTipoUsuario(TipoUsuario.CLIENTE);

        artista = new Usuario();
        artista.setIdUsuario(2L);
        artista.setNombre("Artista Test");
        artista.setEmail("artista@test.com");
        artista.setPassword("password123");
        artista.setTipoUsuario(TipoUsuario.ARTISTA);

        admin = new Usuario();
        admin.setIdUsuario(3L);
        admin.setNombre("Admin Test");
        admin.setEmail("admin@test.com");
        admin.setPassword("password123");
        admin.setTipoUsuario(TipoUsuario.ADMIN);
    }

    private Date getFechaFutura(int dias) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, dias);
        return c.getTime();
    }

    private Comision crearComisionNueva() {
        return service.registrarComision(
            "Comisión de prueba",
            new BigDecimal("100.00"),
            getFechaFutura(15),
            cliente,
            artista
        );
    }

    // TC-02-01: Transición Lógica
    @Test
    public void testTC0201_TransicionLogica_NuevaAEnProgreso() {
        Comision comision = crearComisionNueva();
        assertEquals("Estado inicial debe ser NUEVA", EstadoComision.NUEVA, comision.getEstado());

        Comision actualizada = service.actualizarEstado(
            comision.getIdComision(), 
            EstadoComision.EN_PROGRESO, 
            artista
        );

        assertEquals("Estado debe cambiar a EN_PROGRESO", EstadoComision.EN_PROGRESO, actualizada.getEstado());
        
        List<HistorialAuditoria> historial = service.obtenerHistorial(comision.getIdComision());
        assertFalse("Debe existir historial de auditoría", historial.isEmpty());
        
        HistorialAuditoria ultimoRegistro = historial.get(historial.size() - 1);
        assertEquals("Estado anterior registrado", "NUEVA", ultimoRegistro.getEstadoAnterior());
        assertEquals("Estado nuevo registrado", "EN_PROGRESO", ultimoRegistro.getEstadoNuevo());
        assertNotNull("Debe tener timestamp", ultimoRegistro.getFechaHora());
    }

    @Test
    public void testTC0201_TransicionLogica_EnProgresoAEnRevision() {
        Comision comision = crearComisionNueva();
        service.actualizarEstado(comision.getIdComision(), EstadoComision.EN_PROGRESO, artista);
        
        Comision actualizada = service.actualizarEstado(
            comision.getIdComision(), 
            EstadoComision.EN_REVISION, 
            artista
        );

        assertEquals("Estado debe cambiar a EN_REVISION", EstadoComision.EN_REVISION, actualizada.getEstado());
    }

    // TC-02-02: Estado Terminal
    @Test
    public void testTC0202_EstadoTerminal_Cancelada() {
        Comision comision = crearComisionNueva();
        service.cancelarComision(comision.getIdComision(), "Motivo de cancelación", admin);

        try {
            service.actualizarEstado(comision.getIdComision(), EstadoComision.EN_PROGRESO, artista);
            fail("Debería lanzar excepción por estado terminal");
        } catch (EstadoTerminalException e) {
            assertTrue("Mensaje debe indicar estado terminal", 
                e.getMessage().contains("terminal"));
        }
    }

    @Test
    public void testTC0202_EstadoTerminal_Completada() {
        Comision comision = crearComisionNueva();
        service.actualizarEstado(comision.getIdComision(), EstadoComision.EN_PROGRESO, artista);
        service.actualizarEstado(comision.getIdComision(), EstadoComision.EN_REVISION, artista);
        service.actualizarEstado(comision.getIdComision(), EstadoComision.COMPLETADA, cliente);

        try {
            service.actualizarEstado(comision.getIdComision(), EstadoComision.EN_PROGRESO, artista);
            fail("Debería lanzar excepción por estado terminal");
        } catch (EstadoTerminalException e) {
            assertTrue("Mensaje debe indicar estado terminal", 
                e.getMessage().contains("terminal"));
        }
    }

    // TC-02-03: Notificación (simulada - verificamos cambio de estado)
    @Test
    public void testTC0203_Notificacion_CambioAEnRevision() {
        Comision comision = crearComisionNueva();
        service.actualizarEstado(comision.getIdComision(), EstadoComision.EN_PROGRESO, artista);
        
        Comision actualizada = service.actualizarEstado(
            comision.getIdComision(), 
            EstadoComision.EN_REVISION, 
            artista
        );

        assertEquals("Estado debe ser EN_REVISION", EstadoComision.EN_REVISION, actualizada.getEstado());
        
        List<HistorialAuditoria> historial = service.obtenerHistorial(comision.getIdComision());
        HistorialAuditoria ultimo = historial.get(historial.size() - 1);
        assertEquals("Cliente puede ver cambio en historial", "EN_REVISION", ultimo.getEstadoNuevo());
    }

    // TC-02-04: Habilitar Entrega
    @Test
    public void testTC0204_HabilitarEntrega_EnRevision() {
        Comision comision = crearComisionNueva();
        assertFalse("No debe permitir entregables en estado NUEVA", 
            service.puedeAdjuntarEntregables(comision));

        service.actualizarEstado(comision.getIdComision(), EstadoComision.EN_PROGRESO, artista);
        assertFalse("No debe permitir entregables en EN_PROGRESO", 
            service.puedeAdjuntarEntregables(service.obtenerComision(comision.getIdComision())));

        service.actualizarEstado(comision.getIdComision(), EstadoComision.EN_REVISION, artista);
        assertTrue("Debe permitir entregables en EN_REVISION", 
            service.puedeAdjuntarEntregables(service.obtenerComision(comision.getIdComision())));
    }

    // TC-02-05: Velocidad de Respuesta
    @Test
    public void testTC0205_VelocidadDeRespuesta() {
        Comision comision = crearComisionNueva();
        
        long inicio = System.currentTimeMillis();
        
        service.actualizarEstado(comision.getIdComision(), EstadoComision.EN_PROGRESO, artista);
        
        long fin = System.currentTimeMillis();
        long duracion = fin - inicio;
        
        assertTrue("Actualización debe completarse en menos de 2 segundos", duracion < 2000);
    }

    // Pruebas adicionales de transiciones inválidas
    @Test
    public void testTransicionInvalida_NuevaAEnRevision() {
        Comision comision = crearComisionNueva();
        
        try {
            service.actualizarEstado(comision.getIdComision(), EstadoComision.EN_REVISION, artista);
            fail("No debe permitir saltar de NUEVA a EN_REVISION");
        } catch (TransicionInvalidaException e) {
            assertTrue("Mensaje debe indicar transición inválida", 
                e.getMessage().contains("Transición no permitida"));
        }
    }

    @Test
    public void testTransicionInvalida_NuevaACompletada() {
        Comision comision = crearComisionNueva();
        
        try {
            service.actualizarEstado(comision.getIdComision(), EstadoComision.COMPLETADA, artista);
            fail("No debe permitir saltar de NUEVA a COMPLETADA");
        } catch (TransicionInvalidaException e) {
            assertTrue("Mensaje debe indicar transición inválida", 
                e.getMessage().contains("Transición no permitida"));
        }
    }
}
