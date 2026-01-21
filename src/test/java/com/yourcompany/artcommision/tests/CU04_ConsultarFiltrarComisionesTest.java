package com.yourcompany.artcommision.tests;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.*;

import com.yourcompany.artcommision.model.*;
import com.yourcompany.artcommision.services.ComisionService;

/**
 * CU-04: Consultar y filtrar comisiones
 * Casos de prueba para consulta y filtrado de comisiones
 */
public class CU04_ConsultarFiltrarComisionesTest {

    private ComisionService service;
    private Usuario cliente1;
    private Usuario cliente2;
    private Usuario artista;
    private Usuario admin;

    @Before
    public void setUp() {
        service = new ComisionService();
        
        cliente1 = new Usuario();
        cliente1.setIdUsuario(1L);
        cliente1.setNombre("Cliente Uno");
        cliente1.setEmail("cliente1@test.com");
        cliente1.setPassword("password123");
        cliente1.setTipoUsuario(TipoUsuario.CLIENTE);

        cliente2 = new Usuario();
        cliente2.setIdUsuario(2L);
        cliente2.setNombre("Cliente Dos");
        cliente2.setEmail("cliente2@test.com");
        cliente2.setPassword("password123");
        cliente2.setTipoUsuario(TipoUsuario.CLIENTE);

        artista = new Usuario();
        artista.setIdUsuario(3L);
        artista.setNombre("Artista Test");
        artista.setEmail("artista@test.com");
        artista.setPassword("password123");
        artista.setTipoUsuario(TipoUsuario.ARTISTA);

        admin = new Usuario();
        admin.setIdUsuario(4L);
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

    private Comision crearComision(String descripcion, Usuario cliente, BigDecimal monto) {
        return service.registrarComision(
            descripcion,
            monto,
            getFechaFutura(15),
            cliente,
            artista
        );
    }

    // TC-04-01: Detalle Completo
    @Test
    public void testTC0401_DetalleCompleto() {
        String descripcion = "Ilustración detallada de personaje con armadura";
        BigDecimal monto = new BigDecimal("250.00");
        Date fechaLimite = getFechaFutura(20);

        Comision comision = service.registrarComision(
            descripcion, monto, fechaLimite, cliente1, artista
        );

        Comision detalle = service.obtenerComision(comision.getIdComision());

        assertNotNull("Comisión debe existir", detalle);
        assertEquals("Descripción debe coincidir", descripcion, detalle.getDescripcion());
        assertEquals("Monto debe coincidir", monto, detalle.getMonto());
        assertNotNull("Debe tener fecha límite", detalle.getFechaLimite());
        assertNotNull("Debe tener fecha creación", detalle.getFechaCreacion());
        assertNotNull("Debe tener estado", detalle.getEstado());
        assertNotNull("Debe tener cliente", detalle.getCliente());
        assertNotNull("Debe tener artista", detalle.getArtista());
    }

    // TC-04-02: Filtro de Estado
    @Test
    public void testTC0402_FiltroPorEstado_Canceladas() {
        Comision c1 = crearComision("Comisión 1", cliente1, new BigDecimal("100"));
        Comision c2 = crearComision("Comisión 2", cliente1, new BigDecimal("200"));
        Comision c3 = crearComision("Comisión 3", cliente1, new BigDecimal("300"));

        service.cancelarComision(c1.getIdComision(), "Motivo 1", admin);
        service.cancelarComision(c3.getIdComision(), "Motivo 3", admin);

        List<Comision> canceladas = service.filtrarPorEstado(admin, EstadoComision.CANCELADA);

        assertEquals("Debe haber 2 comisiones canceladas", 2, canceladas.size());
        for (Comision c : canceladas) {
            assertEquals("Todas deben estar CANCELADAS", EstadoComision.CANCELADA, c.getEstado());
        }
    }

    @Test
    public void testTC0402_FiltroPorEstado_EnProgreso() {
        Comision c1 = crearComision("Comisión 1", cliente1, new BigDecimal("100"));
        Comision c2 = crearComision("Comisión 2", cliente1, new BigDecimal("200"));
        crearComision("Comisión 3", cliente1, new BigDecimal("300"));

        service.aprobarComision(c1.getIdComision(), artista);
        service.aprobarComision(c2.getIdComision(), artista);

        List<Comision> enProgreso = service.filtrarPorEstado(admin, EstadoComision.EN_PROGRESO);

        assertEquals("Debe haber 2 comisiones en progreso", 2, enProgreso.size());
    }

    // TC-04-03: Sin Resultados
    @Test
    public void testTC0403_SinResultados() {
        crearComision("Comisión activa", cliente1, new BigDecimal("100"));

        List<Comision> canceladas = service.filtrarPorEstado(admin, EstadoComision.CANCELADA);

        assertTrue("Lista debe estar vacía", canceladas.isEmpty());
    }

    @Test
    public void testTC0403_SinResultados_UsuarioSinComisiones() {
        crearComision("Comisión de cliente 1", cliente1, new BigDecimal("100"));

        List<Comision> comisionesCliente2 = service.consultarComisiones(cliente2);

        assertTrue("Cliente 2 no debe ver comisiones", comisionesCliente2.isEmpty());
    }

    // TC-04-04: Privacidad de Datos
    @Test
    public void testTC0404_PrivacidadDeDatos_ClienteSoloVeSusPropias() {
        crearComision("Comisión Cliente 1 - A", cliente1, new BigDecimal("100"));
        crearComision("Comisión Cliente 1 - B", cliente1, new BigDecimal("150"));
        crearComision("Comisión Cliente 2", cliente2, new BigDecimal("200"));

        List<Comision> comisionesCliente1 = service.consultarComisiones(cliente1);

        assertEquals("Cliente 1 debe ver solo sus 2 comisiones", 2, comisionesCliente1.size());
        for (Comision c : comisionesCliente1) {
            assertEquals("Todas deben pertenecer a cliente 1", 
                cliente1.getIdUsuario(), c.getCliente().getIdUsuario());
        }
    }

    @Test
    public void testTC0404_PrivacidadDeDatos_ArtistaSoloVeSusAsignadas() {
        Usuario artista2 = new Usuario();
        artista2.setIdUsuario(5L);
        artista2.setNombre("Artista Dos");
        artista2.setEmail("artista2@test.com");
        artista2.setTipoUsuario(TipoUsuario.ARTISTA);

        service.registrarComision("Comisión artista 1", new BigDecimal("100"), getFechaFutura(10), cliente1, artista);
        service.registrarComision("Comisión artista 2", new BigDecimal("200"), getFechaFutura(10), cliente1, artista2);

        List<Comision> comisionesArtista1 = service.consultarComisiones(artista);

        assertEquals("Artista debe ver solo 1 comisión asignada", 1, comisionesArtista1.size());
        assertEquals("La comisión debe estar asignada al artista correcto",
            artista.getIdUsuario(), comisionesArtista1.get(0).getArtista().getIdUsuario());
    }

    @Test
    public void testTC0404_PrivacidadDeDatos_AdminVeTodas() {
        crearComision("Comisión Cliente 1", cliente1, new BigDecimal("100"));
        crearComision("Comisión Cliente 2", cliente2, new BigDecimal("200"));

        List<Comision> comisionesAdmin = service.consultarComisiones(admin);

        assertEquals("Admin debe ver todas las comisiones", 2, comisionesAdmin.size());
    }

    // TC-04-05: Acceso Rápido
    @Test
    public void testTC0405_AccesoRapido() {
        Comision comision = crearComision("Comisión para acceso directo", cliente1, new BigDecimal("100"));
        Long idComision = comision.getIdComision();

        long inicio = System.currentTimeMillis();
        
        Comision encontrada = service.obtenerComision(idComision);
        
        long fin = System.currentTimeMillis();

        assertNotNull("Comisión debe encontrarse por ID", encontrada);
        assertEquals("ID debe coincidir", idComision, encontrada.getIdComision());
        assertTrue("Acceso debe ser rápido (< 1 segundo)", (fin - inicio) < 1000);
    }

    @Test
    public void testTC0405_AccesoRapido_ComisionNoExiste() {
        Comision encontrada = service.obtenerComision(99999L);
        
        assertNull("Debe retornar null si no existe", encontrada);
    }

    // Pruebas adicionales de filtrado
    @Test
    public void testFiltradoMultiplesEstados() {
        Comision c1 = crearComision("Nueva 1", cliente1, new BigDecimal("100"));
        Comision c2 = crearComision("Nueva 2", cliente1, new BigDecimal("200"));
        Comision c3 = crearComision("Nueva 3", cliente1, new BigDecimal("300"));

        service.aprobarComision(c2.getIdComision(), artista);
        service.cancelarComision(c3.getIdComision(), "Cancelada", admin);

        List<Comision> nuevas = service.filtrarPorEstado(admin, EstadoComision.NUEVA);
        List<Comision> enProgreso = service.filtrarPorEstado(admin, EstadoComision.EN_PROGRESO);
        List<Comision> canceladas = service.filtrarPorEstado(admin, EstadoComision.CANCELADA);

        assertEquals("1 comisión NUEVA", 1, nuevas.size());
        assertEquals("1 comisión EN_PROGRESO", 1, enProgreso.size());
        assertEquals("1 comisión CANCELADA", 1, canceladas.size());
    }
}
