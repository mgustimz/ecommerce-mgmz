package com.example.ecommercemgmz.auth;

public interface EmailSender {
    /**
     * Sends a transactional email. Implementations may be a dev-only logger or a real SMTP provider.
     * @param to recipient address
     * @param subject email subject
     * @param body plain-text body
     */
    void send(String to, String subject, String body);
}
