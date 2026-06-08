package com.GesCom.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    /**
     * Guarda un archivo en el sistema de archivos local y
     * retorna el nombre único con el que se almacenó.
     */
    String guardar(MultipartFile archivo, Long empresaId);

    /**
     * Lee un archivo del almacenamiento y retorna sus bytes.
     */
    byte[] leer(String nombreAlmacenado);

    /**
     * Elimina un archivo del almacenamiento.
     */
    void eliminar(String nombreAlmacenado);
}
