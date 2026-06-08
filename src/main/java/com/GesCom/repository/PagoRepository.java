package com.GesCom.repository;

import com.GesCom.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {

    List<Pago> findByTransaccion_TransaccionIdOrderByFechaDesc(Long transaccionId);

    boolean existsByTransaccion_TransaccionId(Long transaccionId);

    @org.springframework.data.jpa.repository.Query(
            "SELECT COALESCE(SUM(p.monto), 0) FROM Pago p WHERE p.transaccion.transaccionId = :transaccionId")
    BigDecimal sumMontoByTransaccionId(Long transaccionId);
}
