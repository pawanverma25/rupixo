package dev.pawan.rupixo.common.util;

import java.security.SecureRandom;
import java.util.Base64;

public class RandomizerUtil {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public static String randomBase64(int length){
        byte []bufferArray = new byte[length/2];
        SECURE_RANDOM.nextBytes(bufferArray);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(bufferArray);
    }
}
