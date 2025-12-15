package ar.uba.fi.ingsoft1.sistema_comedores.common.utils;

import java.math.BigInteger;
import java.security.SecureRandom;

public final class Random {

    public static String generateRandomString(int byteSize) {
        SecureRandom random = new SecureRandom();
        byte[] randomBytes = new byte[byteSize];
        random.nextBytes(randomBytes);
        return new BigInteger(1, randomBytes).toString(32);
    }
}
