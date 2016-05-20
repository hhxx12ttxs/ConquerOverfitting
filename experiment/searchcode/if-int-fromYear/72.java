package com.baiken.inventario.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.baiken.framework.report.excel.ICellBuilder;
import com.baiken.inventario.util.BuildReportConteoSolicitudesPorGerenteComercial.TipoReporte;
import com.inventario.dto.Solicitud;
import com.inventario.dto.User;

public class BuildReportConteoSolicitudesPorGerenteComercial implements ICellBuilder {
    public enum TipoReporte {
        ConteoSolicitudesPorSupervisorComercial, ConteoSolicitudesPorIngenieroDT, ConteoOrdenesPorEECC, MontoDesembolsadoPorEECC
    }

    private Set<Integer> years;

    private int rowCount;

    private Map<Integer, Map<Integer, List<BuilderReportDataRow>>> data;

    private TipoReporte tipoReporte;

    private int selectedYear;

    public BuildReportConteoSolicitudesPorGerenteComercial(List<Solicitud> solicitudes, List<User> supervisoresComerciales, List<User> ingenieroDT) {
        years = new HashSet<Integer>();
        data = new HashMap<Integer, Map<Integer, List<BuilderReportDataRow>>>();

        Calendar calendar = Calendar.getInstance();
        int index = 0;
        for (Solicitud solicitud : solicitudes) {
            calendar.setTime(solicitud.getFechaEmision());

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            years.add(year);

            List<BuilderReportDataRow> fromYear;

            {// buscar la fila
                if (!data.containsKey(year)) {
                    data.put(year, new HashMap<Integer, List<BuilderReportDataRow>>());
                    data.get(year).put(0, new ArrayList<BuilderReportDataRow>());
                    data.get(year).put(1, new ArrayList<BuilderReportDataRow>());
                    data.get(year).put(2, new ArrayList<BuilderReportDataRow>());
                    data.get(year).put(3, new ArrayList<BuilderReportDataRow>());
                }
            }
            {// Conteno de solicitudes por supervisor comercial
                List<BuilderReportDataRow> builderReportDataRows = data.get(year).get(0);

                boolean notFound = true;
                for (int i = 0; i < builderReportDataRows.size(); i++) {
                    if (builderReportDataRows.get(i).getRowHeader().equals(solicitud.getSupervisorComercial().getFullName())) {
                        notFound = false;
                        builderReportDataRows.get(i).add(month, 1);
                    }
                }

                if (notFound) {
                    BuilderReportDataRow row = new BuilderReportDataRow();
                    row.setRowHeader(solicitud.getSupervisorComercial().getFullName());
                    row.add(month, 1);
                    builderReportDataRows.add(row);
                }

            }
            System.out.println("BuildReportData.BuildReportData()");
        }
    }

    @Override
    public Object processRow(int row, int col) {
        Object returnValue = data.get(getSelectedYear()).get(getTipoReporte().ordinal()).get(row).toArrayObject()[col];

        if (returnValue.getClass().equals(Double.class)) {
            return (Double)returnValue;
        } else if (returnValue.getClass().equals(Integer.class)) {
            return (Integer) returnValue;
        } else {
            return returnValue;
        }

    }

    public Set<Integer> getYears() {
        return years;
    }

    public void setReporte(TipoReporte tipoReporte) {
        this.tipoReporte = tipoReporte;
    }

    public TipoReporte getTipoReporte() {
        return tipoReporte;
    }

    public void setSelectedYear(int selectedYear) {
        this.selectedYear = selectedYear;
    }

    public int getSelectedYear() {
        return selectedYear;
    }

    public int getRowCount() {
        rowCount = data.get(getSelectedYear()).get(getTipoReporte().ordinal()).size();
        return rowCount;
    }
}

