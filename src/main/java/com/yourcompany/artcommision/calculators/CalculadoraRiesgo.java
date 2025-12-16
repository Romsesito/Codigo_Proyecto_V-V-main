package com.yourcompany.artcommision.calculators;

import java.util.*;
import java.util.concurrent.*;

import com.yourcompany.artcommision.model.*;

public class CalculadoraRiesgo {

    public String determinarNivelRiesgo(Comision comision) {

        if (comision == null || comision.getEstado() == null) {
            return "DESCONOCIDO";
        }

        EstadoComision estado = comision.getEstado();


        if (estado == EstadoComision.CANCELADA) {
            return "INACTIVA";
        }
        if (estado == EstadoComision.COMPLETADA) {
            return "FINALIZADA";
        }

        if (comision.getFechaLimite() == null) {
            return "SIN_FECHA";
        }


        long diffEnMillies = comision.getFechaLimite().getTime() - new Date().getTime();
        long diasRestantes = TimeUnit.DAYS.convert(diffEnMillies, TimeUnit.MILLISECONDS);


        if (diasRestantes < 0) {
   
            if (estado == EstadoComision.NUEVA) {
                return "ABANDONO_CRITICO";
            }
            return "ATRASADA";
        }

  
        if (diasRestantes <= 3) {
 
            if (estado == EstadoComision.NUEVA) {
                return "RIESGO_ALTO";
            }

            if (estado == EstadoComision.EN_REVISION) {
                return "A_TIEMPO";
            }
            return "URGENTE";
        }

        return "NORMAL";
    }
}