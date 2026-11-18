package com.coachgym.coachgym.service;

import com.coachgym.coachgym.model.Asistencia;
import com.coachgym.coachgym.model.Cliente;
import com.coachgym.coachgym.repository.AsistenciaRepository;
import com.coachgym.coachgym.repository.ClienteRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

@Service
public class AsistenciaService {

    private final AsistenciaRepository asistenciaRepo;
    private final ClienteRepository clienteRepo;

    public AsistenciaService(AsistenciaRepository asistenciaRepo, ClienteRepository clienteRepo) {
        this.asistenciaRepo = asistenciaRepo;
        this.clienteRepo = clienteRepo;
    }

    public String registrarEntrada(String codigoRaw) {
        String codigo = codigoRaw.trim().toUpperCase();
        Optional<Cliente> optCliente = clienteRepo.findByCodigo(codigo);

        if (optCliente.isEmpty()) {
            return "ERROR:No existe ningún cliente con el código " + codigo;
        }

        Cliente cliente = optCliente.get();
        LocalDate hoy = LocalDate.now();
        boolean vigente = cliente.getFechaVencimiento() != null && !cliente.getFechaVencimiento().isBefore(hoy);

        // Si ya entró hoy, no se duplica el registro
        if (vigente && asistenciaRepo.existsByClienteIdAndFechaAndPermitidoTrue(cliente.getId(), hoy)) {
            return "OK:✅ " + cliente.getNombre() + " ya había registrado su entrada hoy.";
        }

        Asistencia asistencia = new Asistencia();
        asistencia.setCliente(cliente);
        asistencia.setFecha(hoy);
        asistencia.setHora(LocalTime.now().withNano(0));
        asistencia.setPermitido(vigente);
        asistenciaRepo.save(asistencia);

        if (vigente) {
            return "OK:✅ Bienvenido, " + cliente.getNombre() + " (Vence: " + cliente.getFechaVencimiento() + ")";
        } else {
            return "DENIED:❌ Membresía vencida para " + cliente.getNombre() + " (Venció: " + cliente.getFechaVencimiento() + ")";
        }
    }
}
