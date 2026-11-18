package com.coachgym.coachgym.service;

import com.coachgym.coachgym.model.Clase;
import com.coachgym.coachgym.model.Cliente;
import com.coachgym.coachgym.model.Reserva;
import com.coachgym.coachgym.repository.ClaseRepository;
import com.coachgym.coachgym.repository.ClienteRepository;
import com.coachgym.coachgym.repository.ReservaRepository;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

@Service
public class ReservaService {

    private final ReservaRepository reservaRepo;
    private final ClienteRepository clienteRepo;
    private final ClaseRepository claseRepo;

    public ReservaService(ReservaRepository reservaRepo, ClienteRepository clienteRepo, ClaseRepository claseRepo) {
        this.reservaRepo = reservaRepo;
        this.clienteRepo = clienteRepo;
        this.claseRepo = claseRepo;
    }

    public String crearReserva(Long clienteId, Long claseId, LocalDate fecha) {
        Cliente cliente = clienteRepo.findById(clienteId).orElseThrow();
        Clase clase = claseRepo.findById(claseId).orElseThrow();

        // 1. No se reserva en fechas pasadas
        if (fecha.isBefore(LocalDate.now())) {
            return "ERROR:No se puede reservar en una fecha que ya pasó.";
        }

        // 2. La fecha debe caer el mismo día de la semana en que se da la clase
        String diaDeLaFecha = fecha.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.of("es", "ES"));
        if (!normalizar(diaDeLaFecha).equals(normalizar(clase.getDiaSemana()))) {
            return "ERROR:" + clase.getNombre() + " se imparte los " + clase.getDiaSemana()
                    + ", pero el " + fecha + " es " + diaDeLaFecha + ".";
        }

        // 3. Membresía vigente
        if (cliente.getFechaVencimiento() == null || cliente.getFechaVencimiento().isBefore(fecha)) {
            return "ERROR:El cliente no tiene membresía vigente para esa fecha.";
        }

        // 4. No reservar dos veces la misma clase el mismo día
        if (reservaRepo.existsByClienteIdAndClaseIdAndFechaAndEstado(clienteId, claseId, fecha, "CONFIRMADA")) {
            return "ERROR:El cliente ya tiene reservada esta clase para el " + fecha + ".";
        }

        // 5. Cupo disponible
        long ocupados = reservaRepo.countByClaseIdAndFechaAndEstado(claseId, fecha, "CONFIRMADA");
        if (ocupados >= clase.getCupo()) {
            return "ERROR:Cupo lleno para " + clase.getNombre() + " (" + clase.getCupo() + "/" + clase.getCupo() + ").";
        }

        // Si había una reserva cancelada para ese mismo día, se reactiva (evita choque con la restricción UNIQUE)
        Reserva reserva = reservaRepo.findByClienteIdAndClaseIdAndFecha(clienteId, claseId, fecha)
                .orElse(new Reserva());
        reserva.setCliente(cliente);
        reserva.setClase(clase);
        reserva.setFecha(fecha);
        reserva.setEstado("CONFIRMADA");
        reservaRepo.save(reserva);

        return "OK:Reserva confirmada para " + cliente.getNombre() + " en " + clase.getNombre()
                + " (" + (ocupados + 1) + "/" + clase.getCupo() + " cupos usados).";
    }

    public void cancelarReserva(Long id) {
        reservaRepo.findById(id).ifPresent(r -> {
            r.setEstado("CANCELADA");
            reservaRepo.save(r);
        });
    }

    // Quita tildes y pasa a minúsculas: "Miércoles" -> "miercoles"
    private String normalizar(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase();
    }
}