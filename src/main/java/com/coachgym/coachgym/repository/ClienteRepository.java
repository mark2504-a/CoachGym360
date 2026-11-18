package com.coachgym.coachgym.repository;

import com.coachgym.coachgym.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByCodigo(String codigo);
    List<Cliente> findByNombreContainingIgnoreCaseOrCodigoContainingIgnoreCase(String nombre, String codigo);
    long countByFechaVencimientoGreaterThanEqual(LocalDate fecha);
    long countByFechaVencimientoBetween(LocalDate inicio, LocalDate fin);
}