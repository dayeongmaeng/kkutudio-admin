package com.kkutudio.admin.application;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ManagedAppRepository extends JpaRepository<ManagedApp, Long> {

    List<ManagedApp> findAllByOrderByDisplayOrderAsc();

    Optional<ManagedApp> findByAppCode(String appCode);
}
