package com.coachgym.coachgym.config;

import com.coachgym.coachgym.model.*;
import com.coachgym.coachgym.repository.*;
import com.coachgym.coachgym.service.ClienteService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepo;
    private final PlanRepository planRepo;
    private final ClaseRepository claseRepo;
    private final ClienteRepository clienteRepo;
    private final PagoRepository pagoRepo;
    private final AsistenciaRepository asistenciaRepo;
    private final ClienteService clienteService;
    private final BCryptPasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepo, PlanRepository planRepo, ClaseRepository claseRepo,
                           ClienteRepository clienteRepo, PagoRepository pagoRepo,
                           AsistenciaRepository asistenciaRepo, ClienteService clienteService,
                           BCryptPasswordEncoder passwordEncoder) {
        this.usuarioRepo = usuarioRepo;
        this.planRepo = planRepo;
        this.claseRepo = claseRepo;
        this.clienteRepo = clienteRepo;
        this.pagoRepo = pagoRepo;
        this.asistenciaRepo = asistenciaRepo;
        this.clienteService = clienteService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        crearUsuarios();
        encriptarPasswordsViejas();
        crearPlanes();
        crearClases();
        crearClientesDePrueba();
    }

    // 1. Usuarios por defecto para la demo (con contraseña encriptada)
    private void crearUsuarios() {
        if (usuarioRepo.count() == 0) {
            Usuario admin = new Usuario();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRol("ADMIN");
            usuarioRepo.save(admin);

            Usuario recepcion = new Usuario();
            recepcion.setUsername("recepcion");
            recepcion.setPassword(passwordEncoder.encode("recep123"));
            recepcion.setRol("RECEPCION");
            usuarioRepo.save(recepcion);
        }
    }

    // 2. Si hay contraseñas guardadas en texto plano (versión anterior), se encriptan.
    //    Las encriptadas con BCrypt siempre empiezan con "$2".
    private void encriptarPasswordsViejas() {
        for (Usuario u : usuarioRepo.findAll()) {
            if (!u.getPassword().startsWith("$2")) {
                u.setPassword(passwordEncoder.encode(u.getPassword()));
                usuarioRepo.save(u);
            }
        }
    }

    // 3. Planes base
    private void crearPlanes() {
        if (planRepo.count() == 0) {
            Plan mensual = new Plan();
            mensual.setNombre("Mensual");
            mensual.setPrecio(new BigDecimal("30.00"));
            mensual.setDuracionDias(30);
            planRepo.save(mensual);

            Plan trimestral = new Plan();
            trimestral.setNombre("Trimestral");
            trimestral.setPrecio(new BigDecimal("80.00"));
            trimestral.setDuracionDias(90);
            planRepo.save(trimestral);

            Plan anual = new Plan();
            anual.setNombre("Anual");
            anual.setPrecio(new BigDecimal("290.00"));
            anual.setDuracionDias(365);
            planRepo.save(anual);
        }
    }

    // 4. Clases iniciales
    private void crearClases() {
        if (claseRepo.count() == 0) {
            Clase spinning = new Clase();
            spinning.setNombre("Spinning Intenso");
            spinning.setInstructor("Carlos Méndez");
            spinning.setDiaSemana("Lunes");
            spinning.setHora(LocalTime.of(7, 0));
            spinning.setCupo(15);
            claseRepo.save(spinning);

            Clase crossfit = new Clase();
            crossfit.setNombre("CrossFit Funcional");
            crossfit.setInstructor("Sofía Reyes");
            crossfit.setDiaSemana("Miércoles");
            crossfit.setHora(LocalTime.of(18, 0));
            crossfit.setCupo(12);
            claseRepo.save(crossfit);
        }
    }

    // 5. Clientes de prueba para que el dashboard no salga vacío.
    //    Solo se crean si hay menos de 5 clientes (así no se duplican al reiniciar).
    private void crearClientesDePrueba() {
        if (clienteRepo.count() >= 5) {
            return;
        }

        List<Plan> planes = planRepo.findAll();
        String[] nombres = {
                "María José Hernández", "Carlos Alberto Martínez", "Ana Sofía López",
                "José Roberto Ramírez", "Daniela Beatriz Flores", "Luis Fernando Rivas",
                "Gabriela Alejandra Cruz", "Kevin Ernesto Castillo", "Fátima Guadalupe Díaz",
                "Diego Armando Portillo", "Andrea Michelle Alvarado", "Ricardo José Mejía",
                "Valeria Nicole Romero", "Fernando Antonio Chávez", "Katherine Elizabeth Ayala"
        };
        // Días que le faltan a cada cliente para vencer (negativo = ya venció)
        int[] diasParaVencer = {25, 18, 60, 3, 12, 200, -4, 27, 2, 45, -10, 9, 80, 5, 15};
        String[] metodos = {"Efectivo", "Tarjeta", "Transferencia"};
        LocalDate hoy = LocalDate.now();

        for (int i = 0; i < nombres.length; i++) {
            Cliente cliente = new Cliente();
            cliente.setNombre(nombres[i]);
            cliente.setTelefono("7" + (100 + i) + "-" + (1000 + i * 37));
            cliente = clienteService.guardar(cliente); // genera el código CG-xxxx

            Plan plan = elegirPlan(planes, diasParaVencer[i]);
            LocalDate vence = hoy.plusDays(diasParaVencer[i]);
            LocalDate inicio = vence.minusDays(plan.getDuracionDias());
            cliente.setPlan(plan);
            cliente.setFechaVencimiento(vence);
            clienteRepo.save(cliente);

            Pago pago = new Pago();
            pago.setCliente(cliente);
            pago.setMonto(plan.getPrecio());
            pago.setMetodo(metodos[i % 3]);
            pago.setFechaPago(inicio);
            pago.setPeriodoCubierto(inicio + " al " + vence);
            pagoRepo.save(pago);

            // Los primeros 8 clientes ya llegaron hoy al gym (uno está vencido y se le negó la entrada)
            if (i < 8) {
                Asistencia asistencia = new Asistencia();
                asistencia.setCliente(cliente);
                asistencia.setFecha(hoy);
                asistencia.setHora(LocalTime.of(6, 0).plusMinutes(i * 25L));
                asistencia.setPermitido(!vence.isBefore(hoy));
                asistenciaRepo.save(asistencia);
            }
        }
    }

    // Mensual si le faltan 30 días o menos, Trimestral si son 90 o menos, si no Anual
    private Plan elegirPlan(List<Plan> planes, int dias) {
        int indice = dias <= 30 ? 0 : (dias <= 90 ? 1 : 2);
        return planes.get(Math.min(indice, planes.size() - 1));
    }
}