package com.coachgym.coachgym.repository;

import com.coachgym.coachgym.model.Asistencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {
    List<Asistencia> findByFechaOrderByHoraDesc(LocalDate fecha);
    long countByFechaAndPermitidoTrue(LocalDate fecha);
    boolean existsByClienteIdAndFechaAndPermitidoTrue(Long clienteId, LocalDate fecha);
    boolean existsByClienteId(Long clienteId);
}