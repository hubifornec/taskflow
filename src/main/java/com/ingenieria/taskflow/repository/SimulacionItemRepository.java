package com.ingenieria.taskflow.repository;

import com.ingenieria.taskflow.model.SimulacionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SimulacionItemRepository extends JpaRepository<SimulacionItem, Long> {
    List<SimulacionItem> findBySimulacionId(Long simulacionId);
    List<SimulacionItem> findBySimulacionIdAndEnSprint(Long simulacionId, Boolean enSprint);
}
