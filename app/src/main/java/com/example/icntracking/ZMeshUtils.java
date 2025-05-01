/*
 * Copyright (C) Aer Networks 2024
 * Author: Anders Dam Kofoed
 */
package com.example.icntracking;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;

public class ZMeshUtils {
    
    private static final BigInteger INIT64  = new BigInteger("cbf29ce484222325", 16);
    private static final BigInteger PRIME64 = new BigInteger("100000001b3",      16);
    private static final BigInteger MOD64   = new BigInteger("2").pow(64);
    
    public static String makeTopicHash(String topic) {
        BigInteger hash = INIT64;
        
        for (byte b : topic.getBytes()) {
            hash = hash.xor(BigInteger.valueOf((int) b & 0xff));
            hash = hash.multiply(PRIME64).mod(MOD64);
        }
        
        String hashHexStr = String.format("%016x", hash);
        return hashHexStr.substring(hashHexStr.length()-12, hashHexStr.length());
    }
    
    public static byte[] hexToBytes(String hexString) {
        hexString = hexString.replaceAll("-", "");
        byte[] bytes = new byte[hexString.length() / 2];
        
        for (int i = 0; i < bytes.length; i++) {
            int index = i * 2;
            int j = Integer.parseInt(hexString.substring(index, index + 2), 16);
            bytes[i] = (byte) j;
        }
        return bytes;
    }

    private static final byte[] HEX_ARRAY = "0123456789abcdef".getBytes(StandardCharsets.US_ASCII);
    public static String bytesToHex(byte[] bytes) {
        byte[] hexChars = new byte[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars, StandardCharsets.UTF_8);
    }
    
    // Encipher function (AES-128-CTR)
    static byte[] cryptBytes(byte[] inBytes, int inBytesLen, int fseq, String key, String iv, int cryptMode) {
        byte[] ciphertext = null;
        try {
            // Create the cipher
            Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
            // Create the key
            Key keySpec = new SecretKeySpec(hexToBytes(key), "AES");
            // Generate IV from 9 byte user-IV, 3 byte fseq and 4 byte (counter)
            String fseqStr = String.format("%06x", fseq);
            IvParameterSpec ivSpec = new IvParameterSpec(hexToBytes(iv + fseqStr + "00000000"));
            // Init
            cipher.init(cryptMode, keySpec, ivSpec);
            // Do the magic
            ciphertext = cipher.doFinal(inBytes, 0, inBytesLen);
            
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ciphertext;
    }
  
    public static byte[] encryptBytes(byte[] inBytes, int inBytesLen, int fseq, String key, String iv) {
        return cryptBytes(inBytes, inBytesLen, fseq, key, iv, Cipher.ENCRYPT_MODE);
    }
    public static byte[] decryptBytes(byte[] inBytes, int inBytesLen, int fseq, String key, String iv) {
        return cryptBytes(inBytes, inBytesLen, fseq, key, iv, Cipher.DECRYPT_MODE);
    }

    /**
     * @brief Get the time
     * 
     * @return long time since EPOC in milliseconds
     */
    public static long getEpocTimeMs() {
        return Instant.now().toEpochMilli();
    }


    public static long getPayloadTimestamp(ByteBuffer byteBuf) {
        long timestamp = (byteBuf.get() & 0xFFL) << 40 |
                         (byteBuf.get() & 0xFFL) << 32 |
                         (byteBuf.get() & 0xFFL) << 24 |
                         (byteBuf.get() & 0xFFL) << 16 |
                         (byteBuf.get() & 0xFFL) << 8  |
                         (byteBuf.get() & 0xFFL);

        return timestamp;
    }

    public static long getTimestamp(byte[] byteArr) {
        long timestamp = (byteArr[0] & 0xFFL) << 40 |
                         (byteArr[1] & 0xFFL) << 32 |
                         (byteArr[2] & 0xFFL) << 24 |
                         (byteArr[3] & 0xFFL) << 16 |
                         (byteArr[4] & 0xFFL) << 8  |
                         (byteArr[5] & 0xFFL);

        return timestamp;
    }


}
