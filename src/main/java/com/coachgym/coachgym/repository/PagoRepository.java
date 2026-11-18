package com.coachgym.coachgym.repository;

import com.coachgym.coachgym.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.math.BigDecimal;
import java.util.List;

public interface PagoRepository extends JpaRepository<Pago, Long> {
    List<Pago> findAllByOrderByFechaPagoDescIdDesc();

    @Query("SELECT COALESCE(SUM(p.monto), 0) FROM Pago p")
    BigDecimal sumarIngresosTotales();

    boolean existsByClienteId(Long clienteId);
}

