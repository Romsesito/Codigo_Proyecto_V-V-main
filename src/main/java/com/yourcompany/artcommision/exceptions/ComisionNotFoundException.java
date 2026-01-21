package com.yourcompany.artcommision.exceptions;

public class ComisionNotFoundException extends RuntimeException {
    public ComisionNotFoundException(String message) {
        super(message);
    }
}
