package com.coachgym.coachgym.controller;

import com.coachgym.coachgym.model.Clase;
import com.coachgym.coachgym.model.Plan;
import com.coachgym.coachgym.model.Usuario;
import com.coachgym.coachgym.repository.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final PagoRepository pagoRepo;
    private final PlanRepository planRepo;
    private final ClaseRepository claseRepo;
    private final UsuarioRepository usuarioRepo;
    private final BCryptPasswordEncoder passwordEncoder;

    public AdminController(PagoRepository pagoRepo, PlanRepository planRepo,
                           ClaseRepository claseRepo, UsuarioRepository usuarioRepo,
                           BCryptPasswordEncoder passwordEncoder) {
        this.pagoRepo = pagoRepo;
        this.planRepo = planRepo;
        this.claseRepo = claseRepo;
        this.usuarioRepo = usuarioRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/panel")
    public String panelAdmin(Model model) {
        model.addAttribute("ingresosTotales", pagoRepo.sumarIngresosTotales());
        model.addAttribute("pagos", pagoRepo.findAllByOrderByFechaPagoDescIdDesc());
        model.addAttribute("planes", planRepo.findAll());
        model.addAttribute("clases", claseRepo.findAll());
        model.addAttribute("usuarios", usuarioRepo.findAll());
        return "admin";
    }

    @PostMapping("/planes/guardar")
    public String guardarPlan(@ModelAttribute Plan plan, RedirectAttributes flash) {
        planRepo.save(plan);
        flash.addFlashAttribute("mensaje", "Plan guardado.");
        return "redirect:/admin/panel";
    }

    @PostMapping("/clases/guardar")
    public String guardarClase(@ModelAttribute Clase clase, RedirectAttributes flash) {
        claseRepo.save(clase);
        flash.addFlashAttribute("mensaje", "Clase guardada.");
        return "redirect:/admin/panel";
    }

    @PostMapping("/usuarios/guardar")
    public String guardarUsuario(@ModelAttribute Usuario usuario, RedirectAttributes flash) {
        String username = usuario.getUsername().trim();

        // Evita el error de MySQL por nombre de usuario repetido (columna UNIQUE)
        if (usuarioRepo.findByUsername(username).isPresent()) {
            flash.addFlashAttribute("error", "Ya existe un usuario llamado '" + username + "'.");
            return "redirect:/admin/panel";
        }

        usuario.setUsername(username);
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword())); // se guarda encriptada
        usuarioRepo.save(usuario);
        flash.addFlashAttribute("mensaje", "Usuario creado.");
        return "redirect:/admin/panel";
    }

    @GetMapping("/usuarios/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id, RedirectAttributes flash) {
        usuarioRepo.deleteById(id);
        flash.addFlashAttribute("mensaje", "Usuario eliminado.");
        return "redirect:/admin/panel";
    }
}