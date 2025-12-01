package com.smartgarage.backend.service;
import com.smartgarage.backend.model.Vehicle;
import com.smartgarage.backend.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class VehicleService {
    private final VehicleRepository repo;
    public VehicleService(VehicleRepository repo){ this.repo = repo; }
    public Vehicle save(Vehicle v){ return repo.save(v); }
    public List<Vehicle> byOwner(Long id){ return repo.findByOwnerId(id); }
}
