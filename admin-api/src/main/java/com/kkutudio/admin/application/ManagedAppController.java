package com.kkutudio.admin.application;

import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/applications")
public class ManagedAppController {

    private final ManagedAppRepository managedAppRepository;

    public ManagedAppController(ManagedAppRepository managedAppRepository) {
        this.managedAppRepository = managedAppRepository;
    }

    @GetMapping
    public List<ManagedAppResponse> list() {
        return managedAppRepository.findAllByOrderByDisplayOrderAsc().stream()
                .map(ManagedAppResponse::from)
                .toList();
    }

    @GetMapping("/{appCode}")
    public ManagedAppResponse get(@PathVariable String appCode) {
        return managedAppRepository.findByAppCode(appCode)
                .map(ManagedAppResponse::from)
                .orElseThrow(() -> new NoSuchElementException("No app for code: " + appCode));
    }
}
