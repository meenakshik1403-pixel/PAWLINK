package com.pawlink.common.exception;

/**
 * Thrown when an unverified Sitter or Veterinarian attempts to log in before being approved by an Admin.
 */
public class AccountPendingApprovalException extends RuntimeException {
    public AccountPendingApprovalException(String message) {
        super(message);
    }
}

