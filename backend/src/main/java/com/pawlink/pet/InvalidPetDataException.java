package com.pawlink.pet;

/**
 * Unchecked exception thrown when pet data is invalid, fields are blank,
 * or the species is unknown.
 */
public class InvalidPetDataException extends com.pawlink.common.exception.InvalidPetDataException {
    public InvalidPetDataException(String message) {
        super(message);
    }
}
