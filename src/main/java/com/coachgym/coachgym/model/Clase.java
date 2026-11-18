package com.coachgym.coachgym.model;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "clases")
public class Clase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String nombre; // Ej: Spinning, CrossFit, Yoga

    @Column(nullable = false, length = 80)
    private String instructor;

    @Column(nullable = false, length = 20)
    private String diaSemana; // Lunes, Martes, etc.

    @Column(nullable = false)
    private LocalTime hora;

    @Column(nullable = false)
    private Integer cupo;

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getInstructor() { return instructor; }
    public void setInstructor(String instructor) { this.instructor = instructor; }
    public String getDiaSemana() { return diaSemana; }
    public void setDiaSemana(String diaSemana) { this.diaSemana = diaSemana; }
    public LocalTime getHora() { return hora; }
    public void setHora(LocalTime hora) { this.hora = hora; }
    public Integer getCupo() { return cupo; }
    public void setCupo(Integer cupo) { this.cupo = cupo; }
}