package com.coachgym.coachgym.controller;

import com.coachgym.coachgym.model.Cliente;
import com.coachgym.coachgym.repository.*;
import com.coachgym.coachgym.service.ClienteService;
import com.coachgym.coachgym.service.PagoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
public class GymController {

    private final ClienteService clienteService;
    private final PagoService pagoService;
    private final ClienteRepository clienteRepo;
    private final PlanRepository planRepo;
    private final PagoRepository pagoRepo;
    private final AsistenciaRepository asistenciaRepo;

    public GymController(ClienteService clienteService, PagoService pagoService,
                         ClienteRepository clienteRepo, PlanRepository planRepo,
                         PagoRepository pagoRepo, AsistenciaRepository asistenciaRepo) {
        this.clienteService = clienteService;
        this.pagoService = pagoService;
        this.clienteRepo = clienteRepo;
        this.planRepo = planRepo;
        this.pagoRepo = pagoRepo;
        this.asistenciaRepo = asistenciaRepo;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        LocalDate hoy = LocalDate.now();
        long activos = clienteRepo.countByFechaVencimientoGreaterThanEqual(hoy);
        long porVencer = clienteRepo.countByFechaVencimientoBetween(hoy, hoy.plusDays(5));
        long totalClientes = clienteRepo.count();
        long vencidos = Math.max(0, totalClientes - activos);
        long asistenciasHoy = asistenciaRepo.countByFechaAndPermitidoTrue(hoy);

        model.addAttribute("activos", activos);
        model.addAttribute("porVencer", porVencer);
        model.addAttribute("vencidos", vencidos);
        model.addAttribute("asistenciasHoy", asistenciasHoy);
        model.addAttribute("ultimasAsistencias", asistenciaRepo.findByFechaOrderByHoraDesc(hoy));
        return "dashboard";
    }

    // --- MÓDULO CLIENTES ---
    // Si viene ?editar=ID, el formulario se llena con los datos de ese cliente
    @GetMapping("/clientes")
    public String listarClientes(@RequestParam(required = false) String q,
                                 @RequestParam(required = false) Long editar,
                                 Model model) {
        Cliente formulario = (editar != null) ? clienteService.buscarPorId(editar) : new Cliente();
        model.addAttribute("clientes", clienteService.listar(q));
        model.addAttribute("planes", planRepo.findAll());
        model.addAttribute("clienteForm", formulario);
        model.addAttribute("q", q);
        return "clientes";
    }

    @PostMapping("/clientes/guardar")
    public String guardarCliente(@ModelAttribute Cliente cliente,
                                 @RequestParam(required = false) Long planId,
                                 RedirectAttributes flash) {
        if (planId != null) {
            cliente.setPlan(planRepo.findById(planId).orElse(null));
        }
        Cliente guardado = clienteService.guardar(cliente);
        flash.addFlashAttribute("mensaje", "Cliente guardado: " + guardado.getCodigo() + " - " + guardado.getNombre());
        return "redirect:/clientes";
    }

    @GetMapping("/clientes/eliminar/{id}")
    public String eliminarCliente(@PathVariable Long id, RedirectAttributes flash) {
        if (clienteService.eliminar(id)) {
            flash.addFlashAttribute("mensaje", "Cliente eliminado correctamente.");
        } else {
            flash.addFlashAttribute("error", "No se puede eliminar: el cliente ya tiene pagos, asistencias o reservas registradas.");
        }
        return "redirect:/clientes";
    }

    // --- MÓDULO PAGOS ---
    @GetMapping("/pagos")
    public String vistaPagos(Model model) {
        model.addAttribute("clientes", clienteRepo.findAll());
        model.addAttribute("planes", planRepo.findAll());
        model.addAttribute("pagos", pagoRepo.findAllByOrderByFechaPagoDescIdDesc());
        return "pagos";
    }

    @PostMapping("/pagos/registrar")
    public String registrarPago(@RequestParam Long clienteId,
                                @RequestParam Long planId,
                                @RequestParam String metodo,
                                RedirectAttributes flash) {
        pagoService.registrarPago(clienteId, planId, metodo);
        flash.addFlashAttribute("mensaje", "Pago registrado y membresía actualizada.");
        return "redirect:/pagos";
    }
}