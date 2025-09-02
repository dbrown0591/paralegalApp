package com.paralegal.paralegalApp.util;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.util.Base64;

public class GenJwtSecret {
    public static void main(String[] args) {
        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        String base64 = Base64.getEncoder().encodeToString(key.getEncoded());
        System.out.println("Your JWT Secret (Base64): " + base64);
    }
}
