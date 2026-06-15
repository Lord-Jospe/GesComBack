package com.GesCom.service.Impl;

import com.GesCom.dto.response.AdjuntoResponse;
import com.GesCom.model.Adjunto;
import com.GesCom.model.Transaccion;
import com.GesCom.repository.AdjuntoRepository;
import com.GesCom.repository.TransaccionRepository;
import com.GesCom.service.AdjuntoService;
import com.GesCom.service.FileStorageService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdjuntoServiceImpl implements AdjuntoService {

    private final AdjuntoRepository adjuntoRepository;
    private final TransaccionRepository transaccionRepository;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional
    public AdjuntoResponse subir(Long transaccionId, MultipartFile archivo, Long empresaId) {
        Transaccion t = transaccionRepository
                .findByTransaccionIdAndEmpresa_EmpresaId(transaccionId, empresaId)
                .orElseThrow(() -> new EntityNotFoundException("Transacción no encontrada"));

        String nombreAlmacenado = fileStorageService.guardar(archivo, empresaId);

        Adjunto adjunto = adjuntoRepository.save(Adjunto.builder()
                .transaccion(t)
                .nombreOriginal(archivo.getOriginalFilename())
                .nombreAlmacenado(nombreAlmacenado)
                .tipoArchivo(archivo.getContentType())
                .tamanio(archivo.getSize())
                .build());

        log.info("Adjunto subido: transacción={}, archivo={}", transaccionId, archivo.getOriginalFilename());
        return toResponse(adjunto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdjuntoResponse> listar(Long transaccionId, Long empresaId) {
        transaccionRepository.findByTransaccionIdAndEmpresa_EmpresaId(transaccionId, empresaId)
                .orElseThrow(() -> new EntityNotFoundException("Transacción no encontrada"));

        return adjuntoRepository.findByTransaccion_TransaccionId(transaccionId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdjuntoResponse> listarTodos(Long empresaId) {
        return adjuntoRepository.findByTransaccion_Empresa_EmpresaIdOrderByCreatedAtDesc(empresaId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] descargar(Long adjuntoId, Long empresaId) {
        // Verificar que el adjunto pertenece a una transacción de la empresa
        Adjunto adjunto = adjuntoRepository.findById(adjuntoId)
                .orElseThrow(() -> new EntityNotFoundException("Adjunto no encontrado"));

        if (!adjunto.getTransaccion().getEmpresa().getEmpresaId().equals(empresaId)) {
            throw new IllegalArgumentException("Acceso denegado al archivo");
        }

        return fileStorageService.leer(adjunto.getNombreAlmacenado());
    }

    @Override
    @Transactional
    public void eliminar(Long adjuntoId, Long empresaId) {
        Adjunto adjunto = adjuntoRepository.findById(adjuntoId)
                .orElseThrow(() -> new EntityNotFoundException("Adjunto no encontrado"));

        if (!adjunto.getTransaccion().getEmpresa().getEmpresaId().equals(empresaId)) {
            throw new IllegalArgumentException("Acceso denegado");
        }

        fileStorageService.eliminar(adjunto.getNombreAlmacenado());
        adjuntoRepository.delete(adjunto);
        log.info("Adjunto eliminado: id={}", adjuntoId);
    }

    private AdjuntoResponse toResponse(Adjunto a) {
        return new AdjuntoResponse(
                a.getAdjuntoId(),
                a.getNombreOriginal(),
                a.getTipoArchivo(),
                a.getTamanio(),
                a.getCreatedAt()
        );
    }
}
