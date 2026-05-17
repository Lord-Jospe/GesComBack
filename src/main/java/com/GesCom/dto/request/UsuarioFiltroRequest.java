package com.GesCom.dto.request;

public record UsuarioFiltroRequest(
        Integer pagina,
        Integer tamano,
        Integer rolId,
        String busqueda,
        Boolean soloActivos
) {
    public UsuarioFiltroRequest {
        if (pagina     == null) pagina     = 0;
        if (tamano     == null) tamano     = 10;
        if (soloActivos == null) soloActivos = true;
    }
}
