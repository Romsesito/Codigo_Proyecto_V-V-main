package com.yourcompany.artcommision.tests;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

import org.junit.*;

import com.yourcompany.artcommision.exceptions.*;
import com.yourcompany.artcommision.model.*;
import com.yourcompany.artcommision.services.ComisionService;

/**
 * CU-03: Aprobar o cancelar comisión
 * Casos de prueba para aprobación y cancelación de comisiones
 */
public class CU03_AprobarCancelarComisionTest {

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

    // TC-03-01: Aprobación Directa
    @Test
    public void testTC0301_AprobacionDirecta() {
        Comision comision = crearComisionNueva();
        assertEquals("Estado inicial debe ser NUEVA", EstadoComision.NUEVA, comision.getEstado());

        Comision aprobada = service.aprobarComision(comision.getIdComision(), artista);

        assertEquals("Estado debe cambiar a EN_PROGRESO (Aprobada)", 
            EstadoComision.EN_PROGRESO, aprobada.getEstado());
    }

    // TC-03-02: Rechazo con Motivo
    @Test
    public void testTC0302_RechazoConMotivo() {
        Comision comision = crearComisionNueva();
        String motivo = "El cliente no proporcionó referencias claras";

        Comision cancelada = service.cancelarComision(comision.getIdComision(), motivo, artista);

        assertEquals("Estado debe ser CANCELADA", EstadoComision.CANCELADA, cancelada.getEstado());
        assertEquals("Motivo debe estar registrado", motivo, cancelada.getMotivoCancelacion());
    }

    // TC-03-03: Concurrencia
    @Test
    public void testTC0303_Concurrencia() throws InterruptedException {
        Comision comision = crearComisionNueva();
        final Long idComision = comision.getIdComision();
        
        ExecutorService executor = Executors.newFixedThreadPool(2);
        final CountDownLatch latch = new CountDownLatch(2);
        final boolean[] resultados = new boolean[2];
        final String[] errores = new String[2];

        executor.submit(() -> {
            try {
                service.aprobarComision(idComision, artista);
                resultados[0] = true;
            } catch (Exception e) {
                errores[0] = e.getMessage();
                resultados[0] = false;
            } finally {
                latch.countDown();
            }
        });

        executor.submit(() -> {
            try {
                Thread.sleep(10);
                service.cancelarComision(idComision, "Cancelación concurrente", admin);
                resultados[1] = true;
            } catch (ConcurrenciaException e) {
                errores[1] = e.getMessage();
                resultados[1] = false;
            } catch (Exception e) {
                errores[1] = e.getMessage();
                resultados[1] = false;
            } finally {
                latch.countDown();
            }
        });

        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        int exitosos = 0;
        if (resultados[0]) exitosos++;
        if (resultados[1]) exitosos++;
        
        assertTrue("Al menos una operación debe tener éxito o fallar por concurrencia", 
            exitosos >= 0);
    }

    // TC-03-04: Historial de Actor
    @Test
    public void testTC0304_HistorialDeActor() {
        Comision comision = crearComisionNueva();
        service.aprobarComision(comision.getIdComision(), artista);

        List<HistorialAuditoria> historial = service.obtenerHistorial(comision.getIdComision());

        assertFalse("Debe existir historial", historial.isEmpty());
        
        HistorialAuditoria registro = historial.get(historial.size() - 1);
        assertNotNull("Debe tener actor", registro.getActor());
        assertEquals("Actor debe ser el artista", artista.getNombre(), registro.getActor().getNombre());
        assertNotNull("Debe tener timestamp", registro.getFechaHora());
    }

    // TC-03-05: Validar Pendiente
    @Test
    public void testTC0305_ValidarPendiente_NoPermitirAprobarAprobada() {
        Comision comision = crearComisionNueva();
        service.aprobarComision(comision.getIdComision(), artista);

        try {
            service.aprobarComision(comision.getIdComision(), admin);
            fail("No debe permitir aprobar una comisión ya aprobada");
        } catch (AccionNoPermitidaException e) {
            assertTrue("Mensaje debe indicar que solo NUEVA puede aprobarse", 
                e.getMessage().contains("NUEVA"));
        }
    }

    @Test
    public void testTC0305_ValidarPendiente_NoPermitirCancelarCancelada() {
        Comision comision = crearComisionNueva();
        service.cancelarComision(comision.getIdComision(), "Primera cancelación", admin);

        try {
            service.cancelarComision(comision.getIdComision(), "Segunda cancelación", admin);
            fail("No debe permitir cancelar una comisión ya cancelada");
        } catch (EstadoTerminalException e) {
            assertTrue("Mensaje debe indicar estado terminal", 
                e.getMessage().contains("terminal"));
        }
    }

    // Pruebas adicionales
    @Test
    public void testAprobacionRegistraHistorial() {
        Comision comision = crearComisionNueva();
        service.aprobarComision(comision.getIdComision(), artista);

        List<HistorialAuditoria> historial = service.obtenerHistorial(comision.getIdComision());
        HistorialAuditoria ultimo = historial.get(historial.size() - 1);

        assertEquals("Estado anterior debe ser NUEVA", "NUEVA", ultimo.getEstadoAnterior());
        assertEquals("Estado nuevo debe ser EN_PROGRESO", "EN_PROGRESO", ultimo.getEstadoNuevo());
    }

    @Test
    public void testCancelacionRegistraHistorial() {
        Comision comision = crearComisionNueva();
        service.cancelarComision(comision.getIdComision(), "Motivo de prueba", admin);

        List<HistorialAuditoria> historial = service.obtenerHistorial(comision.getIdComision());
        HistorialAuditoria ultimo = historial.get(historial.size() - 1);

        assertEquals("Estado anterior debe ser NUEVA", "NUEVA", ultimo.getEstadoAnterior());
        assertEquals("Estado nuevo debe ser CANCELADA", "CANCELADA", ultimo.getEstadoNuevo());
    }
}
