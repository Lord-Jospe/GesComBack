package com.GesCom.repository;

import com.GesCom.model.Adjunto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdjuntoRepository extends JpaRepository<Adjunto, Long> {

    List<Adjunto> findByTransaccion_TransaccionId(Long transaccionId);

    List<Adjunto> findByTransaccion_Empresa_EmpresaIdOrderByCreatedAtDesc(Long empresaId);

    Page<Adjunto> findByTransaccion_Empresa_EmpresaIdOrderByCreatedAtDesc(Long empresaId, Pageable pageable);

    void deleteByTransaccion_TransaccionId(Long transaccionId);
}
