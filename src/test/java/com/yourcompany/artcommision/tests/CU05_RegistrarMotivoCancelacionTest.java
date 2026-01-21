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
 * CU-05: Registrar motivo de cancelación
 * Casos de prueba para el registro de motivos de cancelación
 */
public class CU05_RegistrarMotivoCancelacionTest {

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

    // TC-05-01: Texto Requerido
    @Test
    public void testTC0501_TextoRequerido_MotivoVacio() {
        Comision comision = crearComisionNueva();

        try {
            service.cancelarComision(comision.getIdComision(), "", admin);
            fail("Debería lanzar excepción por motivo vacío");
        } catch (MotivoRequeridoException e) {
            assertTrue("Mensaje debe indicar que motivo es requerido", 
                e.getMessage().contains("requerido"));
        }

        Comision sinCancelar = service.obtenerComision(comision.getIdComision());
        assertEquals("Comisión debe seguir en estado NUEVA", EstadoComision.NUEVA, sinCancelar.getEstado());
    }

    @Test
    public void testTC0501_TextoRequerido_MotivoNulo() {
        Comision comision = crearComisionNueva();

        try {
            service.cancelarComision(comision.getIdComision(), null, admin);
            fail("Debería lanzar excepción por motivo nulo");
        } catch (MotivoRequeridoException e) {
            assertTrue("Mensaje debe indicar que motivo es requerido", 
                e.getMessage().contains("requerido"));
        }
    }

    @Test
    public void testTC0501_TextoRequerido_MotivoSoloEspacios() {
        Comision comision = crearComisionNueva();

        try {
            service.cancelarComision(comision.getIdComision(), "   ", admin);
            fail("Debería lanzar excepción por motivo con solo espacios");
        } catch (MotivoRequeridoException e) {
            assertTrue("Mensaje debe indicar que motivo es requerido", 
                e.getMessage().contains("requerido"));
        }
    }

    // TC-05-02: Persistencia de Razón
    @Test
    public void testTC0502_PersistenciaDeRazon() {
        Comision comision = crearComisionNueva();
        String motivoOriginal = "El cliente decidió no continuar con el proyecto por motivos personales";

        service.cancelarComision(comision.getIdComision(), motivoOriginal, admin);

        Comision cancelada = service.obtenerComision(comision.getIdComision());
        assertEquals("Motivo debe persistir exactamente igual", motivoOriginal, cancelada.getMotivoCancelacion());

        List<HistorialAuditoria> historial = service.obtenerHistorial(comision.getIdComision());
        assertFalse("Debe existir registro en historial", historial.isEmpty());
        
        HistorialAuditoria registro = historial.get(historial.size() - 1);
        assertNotNull("Debe tener fecha y hora", registro.getFechaHora());
        assertNotNull("Debe tener usuario actor", registro.getActor());
    }

    // TC-05-03: Notificación al Cliente (verificamos que el motivo esté disponible)
    @Test
    public void testTC0503_NotificacionAlCliente() {
        Comision comision = crearComisionNueva();
        String motivo = "No es posible cumplir con los requerimientos especificados";

        service.cancelarComision(comision.getIdComision(), motivo, artista);

        Comision cancelada = service.obtenerComision(comision.getIdComision());
        
        assertEquals("Estado debe ser CANCELADA", EstadoComision.CANCELADA, cancelada.getEstado());
        assertNotNull("Motivo debe estar disponible para notificación", cancelada.getMotivoCancelacion());
        assertEquals("Motivo debe ser el correcto", motivo, cancelada.getMotivoCancelacion());
        
        assertNotNull("Cliente debe estar asociado para recibir notificación", cancelada.getCliente());
        assertEquals("Cliente correcto asociado", cliente.getIdUsuario(), cancelada.getCliente().getIdUsuario());
    }

    // TC-05-04: Capacidad de Texto
    @Test
    public void testTC0504_CapacidadDeTexto_TextoLargo() {
        Comision comision = crearComisionNueva();
        
        StringBuilder motivoLargo = new StringBuilder();
        motivoLargo.append("Este es un motivo de cancelación muy extenso que incluye múltiples razones: ");
        motivoLargo.append("1. El cliente no proporcionó las referencias necesarias a tiempo. ");
        motivoLargo.append("2. Los requerimientos cambiaron significativamente después de comenzar el trabajo. ");
        motivoLargo.append("3. El presupuesto acordado no cubre las modificaciones solicitadas. ");
        motivoLargo.append("4. Problemas de comunicación durante el proceso creativo. ");
        motivoLargo.append("5. Incompatibilidad de horarios para las revisiones. ");
        motivoLargo.append("Por todas estas razones, ambas partes acordaron cancelar la comisión de manera amistosa.");
        
        String motivoOriginal = motivoLargo.toString();

        service.cancelarComision(comision.getIdComision(), motivoOriginal, admin);

        Comision cancelada = service.obtenerComision(comision.getIdComision());
        assertEquals("Texto largo debe almacenarse completo sin truncar", 
            motivoOriginal, cancelada.getMotivoCancelacion());
        assertEquals("Longitud debe ser idéntica", 
            motivoOriginal.length(), cancelada.getMotivoCancelacion().length());
    }

    @Test
    public void testTC0504_CapacidadDeTexto_TextoConCaracteresEspeciales() {
        Comision comision = crearComisionNueva();
        String motivo = "Cancelación por: \"razones personales\" - incluye acentos (áéíóú) y símbolos @#$%";

        service.cancelarComision(comision.getIdComision(), motivo, admin);

        Comision cancelada = service.obtenerComision(comision.getIdComision());
        assertEquals("Texto con caracteres especiales debe almacenarse correctamente", 
            motivo, cancelada.getMotivoCancelacion());
    }

    // TC-05-05: Abortar Cancelación
    @Test
    public void testTC0505_AbortarCancelacion() {
        Comision comision = crearComisionNueva();
        EstadoComision estadoInicial = comision.getEstado();

        try {
            service.cancelarComision(comision.getIdComision(), "", admin);
        } catch (MotivoRequeridoException e) {
            // Esperado - usuario abortó al no proporcionar motivo
        }

        Comision sinCambios = service.obtenerComision(comision.getIdComision());
        assertEquals("Estado debe permanecer igual", estadoInicial, sinCambios.getEstado());
        assertNull("No debe haber motivo de cancelación", sinCambios.getMotivoCancelacion());
    }

    @Test
    public void testTC0505_AbortarCancelacion_ComisionSiguePendiente() {
        Comision comision = crearComisionNueva();
        assertEquals("Estado inicial debe ser NUEVA (Pendiente)", EstadoComision.NUEVA, comision.getEstado());

        try {
            service.cancelarComision(comision.getIdComision(), null, admin);
        } catch (MotivoRequeridoException e) {
            // Simula que el usuario decidió no continuar
        }

        Comision verificada = service.obtenerComision(comision.getIdComision());
        assertEquals("Comisión debe seguir 'Pendiente' (NUEVA)", EstadoComision.NUEVA, verificada.getEstado());
    }

    // Pruebas adicionales
    @Test
    public void testMotivoSeRegistraEnHistorial() {
        Comision comision = crearComisionNueva();
        String motivo = "Motivo de prueba para historial";

        service.cancelarComision(comision.getIdComision(), motivo, admin);

        List<HistorialAuditoria> historial = service.obtenerHistorial(comision.getIdComision());
        HistorialAuditoria ultimoRegistro = historial.get(historial.size() - 1);

        assertEquals("Estado nuevo debe ser CANCELADA", "CANCELADA", ultimoRegistro.getEstadoNuevo());
        assertEquals("Actor debe ser el admin", admin.getNombre(), ultimoRegistro.getActor().getNombre());
    }

    @Test
    public void testCancelacionDesdeDistintosEstados() {
        Comision comision1 = crearComisionNueva();
        String motivo = "Cancelación de prueba";

        service.cancelarComision(comision1.getIdComision(), motivo, admin);
        assertEquals("Se puede cancelar desde NUEVA", EstadoComision.CANCELADA, 
            service.obtenerComision(comision1.getIdComision()).getEstado());

        Comision comision2 = crearComisionNueva();
        service.aprobarComision(comision2.getIdComision(), artista);
        service.cancelarComision(comision2.getIdComision(), motivo, admin);
        assertEquals("Se puede cancelar desde EN_PROGRESO", EstadoComision.CANCELADA, 
            service.obtenerComision(comision2.getIdComision()).getEstado());
    }
}
