package com.coachgym.coachgym.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RolInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();

        // Permitir acceso libre al login, errores y archivos estáticos
        if (uri.startsWith("/login") || uri.startsWith("/error") || uri.startsWith("/css") || uri.startsWith("/js")) {
            return true;
        }

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuarioLogueado") == null) {
            response.sendRedirect("/login");
            return false;
        }

        String rol = (String) session.getAttribute("rol");

        // Rutas exclusivas de ADMIN (ingresos, planes, clases admin, usuarios y eliminar/anular)
        boolean esRutaAdmin = uri.startsWith("/admin") || uri.contains("/eliminar") || uri.contains("/anular");
        if (esRutaAdmin && !"ADMIN".equals(rol)) {
            response.sendRedirect("/?errorPermiso=true");
            return false;
        }

        return true;
    }
}