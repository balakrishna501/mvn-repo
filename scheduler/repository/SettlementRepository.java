package com.currypoint.scheduler.repository;

import com.currypoint.scheduler.modal.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, String> {
}
