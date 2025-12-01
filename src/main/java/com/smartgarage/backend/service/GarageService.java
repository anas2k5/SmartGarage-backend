package com.smartgarage.backend.service;

import com.smartgarage.backend.model.Garage;
import com.smartgarage.backend.repository.GarageRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GarageService {
    private final GarageRepository garageRepository;

    public GarageService(GarageRepository garageRepository) {
        this.garageRepository = garageRepository;
    }

    public Garage create(Garage g) {
        return garageRepository.save(g);
    }

    public Optional<Garage> findById(Long id) {
        return garageRepository.findById(id);
    }

    public List<Garage> findAll() {
        return garageRepository.findAll();
    }

    public Garage update(Long id, Garage update) {
        return garageRepository.findById(id).map(g -> {
            if (update.getName() != null) g.setName(update.getName());
            if (update.getAddress() != null) g.setAddress(update.getAddress());
            if (update.getPhone() != null) g.setPhone(update.getPhone());
            g.setActive(update.isActive());
            return garageRepository.save(g);
        }).orElseThrow(() -> new RuntimeException("Garage not found"));
    }

    public void setActive(Long id, boolean active) {
        garageRepository.findById(id).ifPresent(g -> {
            g.setActive(active);
            garageRepository.save(g);
        });
    }
}
