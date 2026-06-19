package com.GesCom.service.Impl;

import com.GesCom.model.ComprobantePago;
import com.GesCom.model.Empresa;
import com.GesCom.repository.ComprobantePagoRepository;
import com.GesCom.repository.EmpresaRepository;
import com.GesCom.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final ComprobantePagoRepository comprobanteRepository;
    private final EmpresaRepository empresaRepository;

    @Value("${application.storage.uploads-dir:./uploads}")
    private String uploadsDir;

    private static final String QR_FILENAME = "qr-binance.png";

    @Override
    public byte[] obtenerQR() throws IOException {
        Path qrPath = Paths.get(uploadsDir, QR_FILENAME);
        if (!Files.exists(qrPath)) throw new IOException("QR no encontrado");
        return Files.readAllBytes(qrPath);
    }

    @Override
    public void subirQR(MultipartFile file) throws IOException {
        Path qrPath = Paths.get(uploadsDir, QR_FILENAME);
        Files.createDirectories(qrPath.getParent());
        Files.write(qrPath, file.getBytes());
        log.info("QR de pago actualizado");
    }

    @Override
    @Transactional
    public Map<String, String> subirComprobante(MultipartFile file, Long empresaId) throws IOException {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));

        String nombreUnico = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path dir = Paths.get(uploadsDir, "comprobantes", empresaId.toString());
        Files.createDirectories(dir);
        Files.copy(file.getInputStream(), dir.resolve(nombreUnico), StandardCopyOption.REPLACE_EXISTING);

        comprobanteRepository.save(ComprobantePago.builder()
                .empresa(empresa).nombreArchivo(file.getOriginalFilename())
                .rutaArchivo(nombreUnico).estado("PENDIENTE").build());

        log.info("Comprobante subido: empresa={}, archivo={}", empresaId, file.getOriginalFilename());
        return Map.of("mensaje", "Comprobante subido. Será revisado.");
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> misComprobantes(Long empresaId) {
        return toList(comprobanteRepository.findByEmpresa_EmpresaIdOrderByCreatedAtDesc(empresaId));
    }

    @Override
    public byte[] descargarComprobante(Long comprobanteId) throws IOException {
        ComprobantePago cp = comprobanteRepository.findById(comprobanteId)
                .orElseThrow(() -> new RuntimeException("Comprobante no encontrado"));
        Path path = Paths.get(uploadsDir, "comprobantes",
                cp.getEmpresa().getEmpresaId().toString(), cp.getRutaArchivo());
        return Files.readAllBytes(path);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> comprobantesPendientes() {
        return toList(comprobanteRepository.findByEstadoOrderByCreatedAtDesc("PENDIENTE"));
    }

    @Override
    @Transactional
    public void revisarComprobante(Long comprobanteId, String estado, String notas) {
        ComprobantePago cp = comprobanteRepository.findById(comprobanteId)
                .orElseThrow(() -> new RuntimeException("Comprobante no encontrado"));
        cp.setEstado(estado != null ? estado : "APROBADO");
        if (notas != null) cp.setNotas(notas);
        comprobanteRepository.save(cp);
        log.info("Comprobante {} actualizado a {}", comprobanteId, cp.getEstado());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> estadisticas() {
        return Map.of(
                "pendientes", comprobanteRepository.countByEstado("PENDIENTE"),
                "aprobados", comprobanteRepository.countByEstado("APROBADO")
        );
    }

    private List<Map<String, Object>> toList(List<ComprobantePago> list) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (ComprobantePago cp : list) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("comprobanteId", cp.getComprobanteId());
            m.put("nombreArchivo", cp.getNombreArchivo());
            m.put("empresaId", cp.getEmpresa().getEmpresaId());
            m.put("empresaNombre", cp.getEmpresa().getNombre());
            m.put("estado", cp.getEstado());
            m.put("notas", cp.getNotas());
            m.put("createdAt", cp.getCreatedAt());
            result.add(m);
        }
        return result;
    }
}
