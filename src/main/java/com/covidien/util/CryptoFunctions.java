package com.covidien.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.log4j.Logger;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.salt.ZeroSaltGenerator;

/**
 * CryptoFunctions .
 * 
 * 
 */
public final class CryptoFunctions {

    /**
     * CryptoFunctions.
     */
    
    private CryptoFunctions() {

    }

    /**
     * StandardPBEStringEncryptor .
     */
    private static StandardPBEStringEncryptor configStringEncryptor = null;

    private static final String COV_DMA_PROD_KEY = "COV_DMA_PROD_KEY";

    /**
     * Logger object.
     */
    private static Logger logger = Utility.getLogger(CryptoFunctions.class);

    static {
        String prodKey = System.getenv(COV_DMA_PROD_KEY);
        if (prodKey == null || "".equals(prodKey.trim())) {
            logger.warn("The system environment variable " + COV_DMA_PROD_KEY + " is not set");
        } else {
            configStringEncryptor = new StandardPBEStringEncryptor();
            configStringEncryptor.setSaltGenerator(new ZeroSaltGenerator());
            configStringEncryptor.setPassword(prodKey);
        }
        
    }

    /**
     * Calculate MD5 for an input string .
     * 
     * @param input
     *        .
     * @return String .
     * @throws NoSuchAlgorithmException .
     */
    public static String md5(final String input)
        throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] messageDigest = md.digest(input.getBytes());
        BigInteger number = new BigInteger(1, messageDigest);
        return number.toString(16);
    }

    /**
     * Encrypt a configuration string and return it .
     * 
     * @param in
     *        .
     * @return String .
     */
    public static String encryptConfigString(final String in) {
        // Return the input string if it is null or empty
        if (in == null || in.isEmpty() || configStringEncryptor == null) {
            return in;
        }
        return configStringEncryptor.encrypt(in);
    }
    
    /**
     * Decrypt an encrypted configuration string and return it .
     * 
     * @param in
     *        .
     * @return String .
     */
    public static String decryptConfigString(final String in) {
        // Return the input string if it is null or empty
        if (in == null || in.isEmpty() || configStringEncryptor == null) {
            return in;
        }
        return configStringEncryptor.decrypt(in);
    }
}
