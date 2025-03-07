package com.github.adrienave.booktherook.exception;

public class MissingMandatoryPGNFieldException extends Exception {
    public MissingMandatoryPGNFieldException(String errorMessage) {
        super(errorMessage);
    }
}
