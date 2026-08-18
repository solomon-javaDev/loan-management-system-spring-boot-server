package io.sol.loanmanagementsystemspringbootserver.services;

import io.sol.loanmanagementsystemspringbootserver.entities.SystemSetting;
import io.sol.loanmanagementsystemspringbootserver.repositories.SystemSettingRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SystemSettingService {

    private final SystemSettingRepository repository;

    public SystemSettingService(SystemSettingRepository repository) {
        this.repository = repository;
    }

    public String getSetting(String key, String defaultValue) {
        return repository.findById(key)
                .map(SystemSetting::getValue)
                .orElse(defaultValue);
    }

    public void saveSetting(String key, String value) {
        repository.save(new SystemSetting(key, value));
    }
}
