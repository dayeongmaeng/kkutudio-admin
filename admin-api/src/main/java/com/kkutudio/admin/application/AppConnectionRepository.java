package com.kkutudio.admin.application;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppConnectionRepository extends JpaRepository<AppConnection, Long> {

    Optional<AppConnection> findByApp_AppCode(String appCode);
}
