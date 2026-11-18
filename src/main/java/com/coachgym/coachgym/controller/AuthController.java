package com.coachgym.coachgym.controller;

import com.coachgym.coachgym.model.Usuario;
import com.coachgym.coachgym.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
public class AuthController {

    private final UsuarioRepository usuarioRepo;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthController(UsuarioRepository usuarioRepo, BCryptPasswordEncoder passwordEncoder) {
        this.usuarioRepo = usuarioRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String procesarLogin(@RequestParam String username,
                                @RequestParam String password,
                                HttpSession session,
                                Model model) {
        Optional<Usuario> opt = usuarioRepo.findByUsername(username.trim());
        // matches() encripta lo que escribió el usuario y lo compara con lo guardado
        if (opt.isPresent() && passwordEncoder.matches(password, opt.get().getPassword())) {
            session.setAttribute("usuarioLogueado", opt.get().getUsername());
            session.setAttribute("rol", opt.get().getRol());
            return "redirect:/";
        }
        model.addAttribute("error", "Usuario o contraseña incorrectos");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}