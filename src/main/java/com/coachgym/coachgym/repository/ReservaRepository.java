package com.coachgym.coachgym.repository;

import com.coachgym.coachgym.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    boolean existsByClienteIdAndClaseIdAndFechaAndEstado(Long clienteId, Long claseId, LocalDate fecha, String estado);
    long countByClaseIdAndFechaAndEstado(Long claseId, LocalDate fecha, String estado);
    List<Reserva> findByFechaOrderByIdDesc(LocalDate fecha);
    List<Reserva> findByFechaGreaterThanEqualOrderByFechaAscIdAsc(LocalDate fecha);
    Optional<Reserva> findByClienteIdAndClaseIdAndFecha(Long clienteId, Long claseId, LocalDate fecha);
    boolean existsByClienteId(Long clienteId);
}