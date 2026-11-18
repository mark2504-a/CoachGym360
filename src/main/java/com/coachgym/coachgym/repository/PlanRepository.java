package com.coachgym.coachgym.repository;

import com.coachgym.coachgym.model.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanRepository extends JpaRepository<Plan, Long> {
}