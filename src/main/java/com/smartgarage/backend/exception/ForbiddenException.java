package com.smartgarage.backend.exception;

public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String msg) { super(msg); }
}
