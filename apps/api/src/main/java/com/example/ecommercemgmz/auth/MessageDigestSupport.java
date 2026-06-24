package com.example.ecommercemgmz.auth;

import java.security.MessageDigest;

final class MessageDigestSupport {
    private MessageDigestSupport() {
    }

    static boolean equals(byte[] first, byte[] second) {
        return MessageDigest.isEqual(first, second);
    }
}
