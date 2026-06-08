package com.GesCom.service.Impl;

import com.GesCom.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${application.storage.uploads-dir:./uploads}")
    private String uploadsDir;

    @Override
    public String guardar(MultipartFile archivo, Long empresaId) {
        validarArchivo(archivo);

        String nombreUnico = UUID.randomUUID() + "_" + archivo.getOriginalFilename();
        Path destino = Paths.get(uploadsDir, empresaId.toString(), nombreUnico);

        try {
            Files.createDirectories(destino.getParent());
            Files.copy(archivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
            log.info("Archivo guardado: {}", destino);
            return nombreUnico;
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar el archivo: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] leer(String nombreAlmacenado) {
        // Buscar en subdirectorios de empresa
        try (var stream = Files.walk(Paths.get(uploadsDir), 3)) {
            Path archivo = stream
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().equals(nombreAlmacenado))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Archivo no encontrado: " + nombreAlmacenado));
            return Files.readAllBytes(archivo);
        } catch (IOException e) {
            throw new RuntimeException("Error al leer el archivo: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminar(String nombreAlmacenado) {
        try (var stream = Files.walk(Paths.get(uploadsDir), 3)) {
            stream.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().equals(nombreAlmacenado))
                    .findFirst()
                    .ifPresent(p -> {
                        try {
                            Files.delete(p);
                            log.info("Archivo eliminado: {}", p);
                        } catch (IOException e) {
                            log.warn("No se pudo eliminar el archivo: {}", p);
                        }
                    });
        } catch (IOException e) {
            log.warn("Error al buscar archivo para eliminar: {}", e.getMessage());
        }
    }

    private void validarArchivo(MultipartFile archivo) {
        String tipo = archivo.getContentType();
        if (tipo == null || (!tipo.equals("image/jpeg")
                && !tipo.equals("image/png")
                && !tipo.equals("application/pdf"))) {
            throw new IllegalArgumentException(
                    "Formato no permitido. Solo JPG, PNG y PDF.");
        }
        if (archivo.getSize() > 10 * 1024 * 1024) { // 10 MB
            throw new IllegalArgumentException(
                    "El archivo excede el tamaño máximo de 10 MB.");
        }
    }
}
