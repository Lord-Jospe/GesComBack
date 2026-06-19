package com.GesCom.service;

import com.GesCom.dto.response.*;

import java.time.LocalDate;
import java.util.List;

public interface ReportePdfService {
    byte[] generarLibroDiario(List<AsientoResponse> asientos, LocalDate desde, LocalDate hasta);
    byte[] generarLibroMayor(LibroMayorResponse data);
    byte[] generarEstadoResultados(EstadoResultadosResponse data);
    byte[] generarBalanceGeneral(BalanceGeneralResponse data);
    byte[] generarNomina(List<NominaResponse> nominas);
}
