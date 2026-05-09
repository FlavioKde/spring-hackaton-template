package com.interhack.spring_hackaton_template.repository;

import com.interhack.spring_hackaton_template.model.Pallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PalletRepository extends JpaRepository<Pallet, Long> {
    List<Pallet> findByLoadPlanIdOrderByPalletNumberAsc(Long loadPlanId);
    List<Pallet> findByLoadPlanIdAndLoadedFalse(Long loadPlanId);
}
