package com.coachgym.coachgym.controller;

import com.coachgym.coachgym.repository.*;
import com.coachgym.coachgym.service.AsistenciaService;
import com.coachgym.coachgym.service.ReservaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
public class OperacionController {

    private final AsistenciaService asistenciaService;
    private final ReservaService reservaService;
    private final AsistenciaRepository asistenciaRepo;
    private final ClienteRepository clienteRepo;
    private final ClaseRepository claseRepo;
    private final ReservaRepository reservaRepo;

    public OperacionController(AsistenciaService asistenciaService, ReservaService reservaService,
                               AsistenciaRepository asistenciaRepo, ClienteRepository clienteRepo,
                               ClaseRepository claseRepo, ReservaRepository reservaRepo) {
        this.asistenciaService = asistenciaService;
        this.reservaService = reservaService;
        this.asistenciaRepo = asistenciaRepo;
        this.clienteRepo = clienteRepo;
        this.claseRepo = claseRepo;
        this.reservaRepo = reservaRepo;
    }

    // --- MÓDULO ASISTENCIA (QR / CÓDIGO) ---
    @GetMapping("/asistencia")
    public String vistaAsistencia(Model model) {
        model.addAttribute("asistenciasHoy", asistenciaRepo.findByFechaOrderByHoraDesc(LocalDate.now()));
        return "asistencia";
    }

    @PostMapping("/asistencia/marcar")
    public String marcarAsistencia(@RequestParam String codigo, RedirectAttributes flash) {
        String resultado = asistenciaService.registrarEntrada(codigo);
        String[] partes = resultado.split(":", 2);
        flash.addFlashAttribute("tipoAlerta", partes[0]); // OK, DENIED o ERROR
        flash.addFlashAttribute("textoAlerta", partes[1]);
        return "redirect:/asistencia";
    }

    // --- MÓDULO RESERVAS ---
    @GetMapping("/reservas")
    public String vistaReservas(Model model) {
        LocalDate hoy = LocalDate.now();
        model.addAttribute("clientes", clienteRepo.findAll());
        model.addAttribute("clases", claseRepo.findAll());
        model.addAttribute("proximasReservas", reservaRepo.findByFechaGreaterThanEqualOrderByFechaAscIdAsc(hoy));
        model.addAttribute("hoy", hoy);
        return "reservas";
    }

    @PostMapping("/reservas/crear")
    public String crearReserva(@RequestParam Long clienteId,
                               @RequestParam Long claseId,
                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                               RedirectAttributes flash) {
        String resultado = reservaService.crearReserva(clienteId, claseId, fecha);
        String[] partes = resultado.split(":", 2);
        flash.addFlashAttribute("tipoAlerta", partes[0]);
        flash.addFlashAttribute("textoAlerta", partes[1]);
        return "redirect:/reservas";
    }

    @GetMapping("/reservas/cancelar/{id}")
    public String cancelarReserva(@PathVariable Long id, RedirectAttributes flash) {
        reservaService.cancelarReserva(id);
        flash.addFlashAttribute("tipoAlerta", "OK");
        flash.addFlashAttribute("textoAlerta", "Reserva cancelada.");
        return "redirect:/reservas";
    }
}