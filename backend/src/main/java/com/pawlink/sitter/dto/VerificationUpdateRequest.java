package com.pawlink.sitter.dto;

/**
 * Payload for updating a sitter's verification status.
 */
public class VerificationUpdateRequest {

    private boolean verified;

    public VerificationUpdateRequest() {
    }

    public VerificationUpdateRequest(boolean verified) {
        this.verified = verified;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }
}

