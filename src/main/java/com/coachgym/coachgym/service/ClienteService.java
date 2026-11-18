package com.coachgym.coachgym.service;

import com.coachgym.coachgym.model.Cliente;
import com.coachgym.coachgym.repository.AsistenciaRepository;
import com.coachgym.coachgym.repository.ClienteRepository;
import com.coachgym.coachgym.repository.PagoRepository;
import com.coachgym.coachgym.repository.ReservaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepo;
    private final PagoRepository pagoRepo;
    private final AsistenciaRepository asistenciaRepo;
    private final ReservaRepository reservaRepo;

    public ClienteService(ClienteRepository clienteRepo, PagoRepository pagoRepo,
                          AsistenciaRepository asistenciaRepo, ReservaRepository reservaRepo) {
        this.clienteRepo = clienteRepo;
        this.pagoRepo = pagoRepo;
        this.asistenciaRepo = asistenciaRepo;
        this.reservaRepo = reservaRepo;
    }

    public List<Cliente> listar(String filtro) {
        if (filtro != null && !filtro.isBlank()) {
            return clienteRepo.findByNombreContainingIgnoreCaseOrCodigoContainingIgnoreCase(filtro, filtro);
        }
        return clienteRepo.findAll();
    }

    public Cliente guardar(Cliente cliente) {
        if (cliente.getId() == null) {
            cliente.setCodigo("TMP-" + java.util.UUID.randomUUID().toString().substring(0, 12));
            Cliente guardado = clienteRepo.save(cliente);
            guardado.setCodigo(String.format("CG-%04d", guardado.getId()));
            return clienteRepo.save(guardado);
        }
        Cliente existente = clienteRepo.findById(cliente.getId()).orElseThrow();
        existente.setNombre(cliente.getNombre());
        existente.setTelefono(cliente.getTelefono());
        if (cliente.getPlan() != null) {
            existente.setPlan(cliente.getPlan());
        }
        return clienteRepo.save(existente);
    }

    public Cliente buscarPorId(Long id) {
        return clienteRepo.findById(id).orElseThrow();
    }

    /** Devuelve true si se eliminó; false si tiene historial y no se puede borrar. */
    public boolean eliminar(Long id) {
        boolean tieneHistorial = pagoRepo.existsByClienteId(id)
                || asistenciaRepo.existsByClienteId(id)
                || reservaRepo.existsByClienteId(id);
        if (tieneHistorial) {
            return false;
        }
        clienteRepo.deleteById(id);
        return true;
    }
}