package com.coachgym.coachgym.service;

import com.coachgym.coachgym.model.Cliente;
import com.coachgym.coachgym.model.Pago;
import com.coachgym.coachgym.model.Plan;
import com.coachgym.coachgym.repository.ClienteRepository;
import com.coachgym.coachgym.repository.PagoRepository;
import com.coachgym.coachgym.repository.PlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;

@Service
public class PagoService {

    private final PagoRepository pagoRepo;
    private final ClienteRepository clienteRepo;
    private final PlanRepository planRepo;

    public PagoService(PagoRepository pagoRepo, ClienteRepository clienteRepo, PlanRepository planRepo) {
        this.pagoRepo = pagoRepo;
        this.clienteRepo = clienteRepo;
        this.planRepo = planRepo;
    }

    @Transactional
    public void registrarPago(Long clienteId, Long planId, String metodo) {
        Cliente cliente = clienteRepo.findById(clienteId).orElseThrow();
        Plan plan = planRepo.findById(planId).orElseThrow();
        LocalDate hoy = LocalDate.now();

        // Regla del plan: si está vigente suma desde su vencimiento; si venció, desde hoy
        LocalDate fechaBase = (cliente.getFechaVencimiento() != null && !cliente.getFechaVencimiento().isBefore(hoy))
                ? cliente.getFechaVencimiento()
                : hoy;

        LocalDate nuevoVencimiento = fechaBase.plusDays(plan.getDuracionDias());

        cliente.setPlan(plan);
        cliente.setFechaVencimiento(nuevoVencimiento);
        clienteRepo.save(cliente);

        Pago pago = new Pago();
        pago.setCliente(cliente);
        pago.setMonto(plan.getPrecio());
        pago.setMetodo(metodo);
        pago.setFechaPago(hoy);
        pago.setPeriodoCubierto(fechaBase + " al " + nuevoVencimiento);
        pagoRepo.save(pago);
    }
}