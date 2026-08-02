package com.sangam.abcbank.emiservice.repository;

import com.sangam.abcbank.emiservice.entity.EmiTransition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmiTransitionRepository extends JpaRepository<EmiTransition, Long> {
    List<EmiTransition> findByUsername(String username);
    List<EmiTransition> findByUsernameAndSourceType(String username, EmiTransition.SourceType sourceType);
}
