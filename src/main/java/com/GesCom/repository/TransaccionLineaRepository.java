package com.GesCom.repository;

import com.GesCom.model.TransaccionLinea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransaccionLineaRepository extends JpaRepository<TransaccionLinea, Long> {

    List<TransaccionLinea> findByTransaccion_TransaccionId(Long transaccionId);

    void deleteByTransaccion_TransaccionId(Long transaccionId);
}
