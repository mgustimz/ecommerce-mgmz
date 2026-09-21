package com.example.ecommercemgmz.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Dev email delivery: logs the message instead of sending it.
 * The reset link appears in the application log under "DEV EMAIL".
 */
@Component
@ConditionalOnProperty(name = "app.mail.mode", havingValue = "log", matchIfMissing = true)
@Slf4j
public class LoggingEmailSender implements EmailSender {
    @Override
    public void send(String to, String subject, String body) {
        log.info("DEV EMAIL -> to={} | subject={} | body={}", to, subject, body);
    }
}
