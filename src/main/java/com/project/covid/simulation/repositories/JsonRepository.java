package com.project.covid.simulation.repositories;

import com.project.covid.simulation.models.JsonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JsonRepository extends JpaRepository<JsonEntity, Long> {
}
