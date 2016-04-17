/*
 * Copyright 2013 Lorenzo González.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package es.logongas.encuestas.modelo.resultados;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.apache.commons.math3.distribution.TDistribution;

/**
 * Información sobre inferencia estadística
 * @author Lorenzo González
 */
public class InferenciaEstadistica {
    private IntervaloConfianza intervaloConfianzaMedia;
    private int numDecimals;

    public InferenciaEstadistica(EstadisticaDescriptiva estadisticaDescriptiva,BigDecimal nivelConfianza,int numDecimals) {
        this.numDecimals=numDecimals;
        if (nivelConfianza.compareTo(BigDecimal.ZERO)<=0) {
            throw new IllegalArgumentException("El nivelConfianza debe ser mayor que 0");
        }
        if (nivelConfianza.compareTo(BigDecimal.ONE)>=0) {
            throw new IllegalArgumentException("El nivelConfianza debe ser menor que 1");
        }

        TDistribution tDistribution=new TDistribution(estadisticaDescriptiva.getNumMuestras()-1);
        double t=tDistribution.inverseCumulativeProbability(nivelConfianza.doubleValue());
        BigDecimal delta= new BigDecimal(t*(estadisticaDescriptiva.getDesviacionEstandar().doubleValue()/Math.sqrt(estadisticaDescriptiva.getNumMuestras())));

        BigDecimal min=estadisticaDescriptiva.getMedia().subtract(delta).setScale(this.numDecimals, RoundingMode.HALF_UP);
        BigDecimal max=estadisticaDescriptiva.getMedia().add(delta).setScale(this.numDecimals, RoundingMode.HALF_UP);
        intervaloConfianzaMedia=new IntervaloConfianza(min, max, nivelConfianza);

    }

    /**
     * @return the intervaloConfianzaMedia
     */
    public IntervaloConfianza getIntervaloConfianzaMedia() {
        return intervaloConfianzaMedia;
    }
}

