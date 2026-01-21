package com.yourcompany.artcommision.tests;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import org.junit.*;

import com.yourcompany.artcommision.exceptions.*;
import com.yourcompany.artcommision.model.*;
import com.yourcompany.artcommision.services.ComisionService;

/**
 * CU-01: Registrar comisión
 * Casos de prueba para el registro de nuevas comisiones
 */
public class CU01_RegistrarComisionTest {

    private ComisionService service;
    private Usuario clienteValido;
    private Usuario artistaValido;

    @Before
    public void setUp() {
        service = new ComisionService();
        
        clienteValido = new Usuario();
        clienteValido.setIdUsuario(1L);
        clienteValido.setNombre("Cliente Test");
        clienteValido.setEmail("cliente@test.com");
        clienteValido.setPassword("password123");
        clienteValido.setTipoUsuario(TipoUsuario.CLIENTE);

        artistaValido = new Usuario();
        artistaValido.setIdUsuario(2L);
        artistaValido.setNombre("Artista Test");
        artistaValido.setEmail("artista@test.com");
        artistaValido.setPassword("password123");
        artistaValido.setTipoUsuario(TipoUsuario.ARTISTA);
        artistaValido.setEspecialidad("Ilustración Digital");
    }

    private Date getFechaFutura(int dias) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, dias);
        return c.getTime();
    }

    // TC-01-01: Registro Exitoso
    @Test
    public void testTC0101_RegistroExitoso() {
        String descripcion = "Ilustración de personaje anime";
        BigDecimal monto = new BigDecimal("150.00");
        Date fechaLimite = getFechaFutura(15);

        Comision comision = service.registrarComision(
            descripcion, monto, fechaLimite, clienteValido, artistaValido
        );

        assertNotNull("La comisión debe crearse", comision);
        assertNotNull("Debe tener ID asignado", comision.getIdComision());
        assertEquals("Estado debe ser NUEVA (Pendiente)", EstadoComision.NUEVA, comision.getEstado());
        assertEquals("Descripción debe coincidir", descripcion, comision.getDescripcion());
        assertEquals("Monto debe coincidir", monto, comision.getMonto());
        assertNotNull("Debe tener fecha de creación", comision.getFechaCreacion());
    }

    // TC-01-02: Campos Vacíos
    @Test
    public void testTC0102_CamposVacios_DescripcionVacia() {
        try {
            service.registrarComision(null, new BigDecimal("100"), getFechaFutura(10), clienteValido, artistaValido);
            fail("Debería lanzar excepción por campo vacío");
        } catch (CamposObligatoriosException e) {
            assertTrue("Mensaje debe indicar campos obligatorios", 
                e.getMessage().contains("obligatorios"));
        }
    }

    @Test
    public void testTC0102_CamposVacios_MontoVacio() {
        try {
            service.registrarComision("Descripción válida", null, getFechaFutura(10), clienteValido, artistaValido);
            fail("Debería lanzar excepción por monto vacío");
        } catch (CamposObligatoriosException e) {
            assertTrue("Mensaje debe indicar campos obligatorios", 
                e.getMessage().contains("monto"));
        }
    }

    @Test
    public void testTC0102_CamposVacios_ClienteVacio() {
        try {
            service.registrarComision("Descripción válida", new BigDecimal("100"), getFechaFutura(10), null, artistaValido);
            fail("Debería lanzar excepción por cliente vacío");
        } catch (CamposObligatoriosException e) {
            assertTrue("Mensaje debe indicar campos obligatorios", 
                e.getMessage().contains("cliente"));
        }
    }

    // TC-01-03: Tiempo de Carga
    @Test
    public void testTC0103_TiempoDeCarga() {
        long inicio = System.currentTimeMillis();
        
        Comision comision = service.registrarComision(
            "Test de rendimiento", 
            new BigDecimal("200.00"), 
            getFechaFutura(20),
            clienteValido, 
            artistaValido
        );
        
        long fin = System.currentTimeMillis();
        long duracion = fin - inicio;
        
        assertNotNull("Comisión debe crearse", comision);
        assertTrue("Registro debe completarse en menos de 3 segundos", duracion < 3000);
    }

    // TC-01-04: Validar Participante
    @Test
    public void testTC0104_ValidarParticipante_ClienteValido() {
        assertTrue("Cliente válido debe pasar validación", 
            service.validarParticipante(clienteValido));
    }

    @Test
    public void testTC0104_ValidarParticipante_ArtistaValido() {
        assertTrue("Artista válido debe pasar validación", 
            service.validarParticipante(artistaValido));
    }

    @Test
    public void testTC0104_ValidarParticipante_UsuarioSinNombre() {
        Usuario usuarioInvalido = new Usuario();
        usuarioInvalido.setEmail("test@test.com");
        usuarioInvalido.setTipoUsuario(TipoUsuario.CLIENTE);
        
        assertFalse("Usuario sin nombre no debe pasar validación", 
            service.validarParticipante(usuarioInvalido));
    }

    @Test
    public void testTC0104_ValidarParticipante_UsuarioSinEmail() {
        Usuario usuarioInvalido = new Usuario();
        usuarioInvalido.setNombre("Test");
        usuarioInvalido.setTipoUsuario(TipoUsuario.CLIENTE);
        
        assertFalse("Usuario sin email no debe pasar validación", 
            service.validarParticipante(usuarioInvalido));
    }

    @Test
    public void testTC0104_ValidarParticipante_UsuarioNulo() {
        assertFalse("Usuario nulo no debe pasar validación", 
            service.validarParticipante(null));
    }

    // TC-01-05: Error de Conexión
    @Test
    public void testTC0105_ErrorDeConexion() {
        service.setConexionActiva(false);
        
        try {
            service.registrarComision(
                "Test conexión", 
                new BigDecimal("100"), 
                getFechaFutura(10),
                clienteValido, 
                artistaValido
            );
            fail("Debería lanzar excepción por error de conexión");
        } catch (DatabaseConnectionException e) {
            assertTrue("Mensaje debe indicar reintento", 
                e.getMessage().contains("Intente nuevamente más tarde"));
        }
    }
}
