package com.coachgym.coachgym.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private String codigo; // Ej: CG-0001

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 20)
    private String telefono;

    @ManyToOne
    @JoinColumn(name = "plan_id")
    private Plan plan;

    private LocalDate fechaVencimiento;

    // Estado calculado para la vista (ACTIVO, POR_VENCER, VENCIDO)
    @Transient
    public String getEstado() {
        if (fechaVencimiento == null) return "VENCIDO";
        LocalDate hoy = LocalDate.now();
        if (fechaVencimiento.isBefore(hoy)) return "VENCIDO";
        long diasRestantes = ChronoUnit.DAYS.between(hoy, fechaVencimiento);
        if (diasRestantes <= 5) return "POR_VENCER";
        return "ACTIVO";
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public Plan getPlan() { return plan; }
    public void setPlan(Plan plan) { this.plan = plan; }
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }
}