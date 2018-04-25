package com.sansara.develop.innocrypt.util;

import com.sansara.develop.innocrypt.data.StaticConfig;

import java.util.Random;

public class EncryptingUtils {

    public static String generateKey(int size) {
        Random random = new Random();
        char[] key = new char[size];
        for (int i = 0; i < size; i++) {
            key[i] = (char) random.nextInt(Character.MAX_VALUE);
        }
        return new String(key);
    }

    public static String encryptVernam(String plainText, String key) {
        char[] plainTextTemp = plainText.toCharArray();
        char[] keyTemp = key.toCharArray();
        for (int i = 0; i < plainText.length(); i++) {
            plainTextTemp[i] = (char) (plainTextTemp[i] ^ keyTemp[i % StaticConfig.SIZE_OF_ENCRYPTING_KEY]);
        }
        return new String(plainTextTemp);
    }

    public static String decryptVernam(String encryptedText, String key) {
        char[] encryptedTextTemp = encryptedText.toCharArray();
        char[] keyTemp = key.toCharArray();
        for (int i = 0; i < encryptedText.length(); i++) {
            encryptedTextTemp[i] = (char) (encryptedTextTemp[i] ^ keyTemp[i % StaticConfig.SIZE_OF_ENCRYPTING_KEY]);
        }
        return new String(encryptedTextTemp);
    }
}
