package com.smartgarage.backend.exception;

public class VehicleAlreadyExistsException extends RuntimeException {
    public VehicleAlreadyExistsException(String plate) {
        super("Vehicle with this plate number already exists: " + plate);
    }
}
