package com.yourcompany.artcommision.calculators;

import static org.junit.Assert.assertEquals;

import java.util.*;

import org.junit.*;

import com.yourcompany.artcommision.calculators.*;
import com.yourcompany.artcommision.model.*;

public class CalculadoraRiesgoTest {

    private CalculadoraRiesgo calculadora = new CalculadoraRiesgo();


    private Date getFechaRelativa(int dias) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, dias);
        return c.getTime();
    }

    @Test
    public void testEstadoCancelada() {
        Comision c = new Comision();
        c.setEstado(EstadoComision.CANCELADA);
        assertEquals("INACTIVA", calculadora.determinarNivelRiesgo(c));
    }

    @Test
    public void testEstadoCompletada() {
        Comision c = new Comision();
        c.setEstado(EstadoComision.COMPLETADA);
        assertEquals("FINALIZADA", calculadora.determinarNivelRiesgo(c));
    }

    @Test
    public void testVencidaCriticoAbandono() {
        Comision c = new Comision();
        c.setEstado(EstadoComision.NUEVA);
        c.setFechaLimite(getFechaRelativa(-5)); 
        assertEquals("CRITICO_ABANDONO", calculadora.determinarNivelRiesgo(c));
    }

    @Test
    public void testVencidaAtrasada() {
        Comision c = new Comision();
        c.setEstado(EstadoComision.EN_PROGRESO);
        c.setFechaLimite(getFechaRelativa(-1));
        assertEquals("ATRASADA", calculadora.determinarNivelRiesgo(c));
    }

    @Test
    public void testCercanaRiesgoAlto() {
        Comision c = new Comision();
        c.setEstado(EstadoComision.NUEVA);
        c.setFechaLimite(getFechaRelativa(2));
        assertEquals("RIESGO_ALTO", calculadora.determinarNivelRiesgo(c));
    }

    @Test
    public void testCercanaEnRevisionSalvada() {
        Comision c = new Comision();
        c.setEstado(EstadoComision.EN_REVISION);
        c.setFechaLimite(getFechaRelativa(1));
        assertEquals("A_TIEMPO", calculadora.determinarNivelRiesgo(c));
    }

    @Test
    public void testCercanaUrgente() {
        Comision c = new Comision();
        c.setEstado(EstadoComision.EN_PROGRESO);
        c.setFechaLimite(getFechaRelativa(3));
        assertEquals("URGENTE", calculadora.determinarNivelRiesgo(c));
    }

    @Test
    public void testFuturoNormal() {
        Comision c = new Comision();
        c.setEstado(EstadoComision.EN_PROGRESO);
        c.setFechaLimite(getFechaRelativa(10)); 
        assertEquals("NORMAL", calculadora.determinarNivelRiesgo(c));
    }
}