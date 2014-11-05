package com.covidien.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * Load the properties from the given file.
 * 
 * @author root
 */
public final class LoadProperties {
    private static final Logger logger = Utility.getLogger(LoadProperties.class);

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    
    /**
     * Private constructor to avoid instantiating the class.
     */
    private LoadProperties() {

    }

    /**
     * Create a Properties object and return the same.
     * 
     * @param encryptedPropertiesFile
     *        encrypted file from to load the properties.
     * @return Properties object
     */
    @SuppressWarnings("static-access")
    public static Properties loadEncryptedProperties(final String encryptedPropertiesFile) {
        Properties properties = new Properties();
        InputStream in = null;
        BufferedReader br = null;
        InputStream is = null;
        try {
            File path = new File(encryptedPropertiesFile);
            if (path != null && path.exists()) {
                in = new FileInputStream(path);
            } else {
                in = LoadProperties.class.getClassLoader().getSystemResourceAsStream(encryptedPropertiesFile);
            }

            if (in != null) {
                br = new BufferedReader(new InputStreamReader(in));
                String line;
                StringBuilder sb = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    sb.append(CryptoFunctions.decryptConfigString(line)).append(LINE_SEPARATOR);
                }
                is = new ByteArrayInputStream(sb.toString().getBytes());
                properties.load(is);
            } else {
                logger.error("The encryted configuration file is not found.");
            }
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage());
        } catch (IOException e) {
            logger.error(e.getMessage());
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    logger.error(e.getMessage());
                }
            }

            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    logger.error(e.getMessage());
                }
            }

            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    logger.error(e.getMessage());
                }
            }
        }
        return properties;
    }

    /**
     * Create a Properties object and return the same.
     * 
     * @param propertiesFile
     *        file from to load the properties
     * @return Properties object
     */
    @SuppressWarnings("static-access")
    public static Properties loadProperties(final String propertiesFile) {

        Properties properties = new Properties();
        FileInputStream in = null;
        try {
            // load a properties file
            File path = new File(propertiesFile);
            if (path.exists()) {
                in = new FileInputStream(path);
                properties.load(in);
            }
            properties.setProperty("currentPath", currentPath());
        } catch (FileNotFoundException e) {
            try {
                properties.load(LoadProperties.class.getClassLoader().getSystemResourceAsStream(propertiesFile));
            } catch (IOException e1) {
                logger.debug("Cannot load property:" + propertiesFile, e1);
            }
        } catch (IOException e) {
            try {
                properties.load(LoadProperties.class.getClassLoader().getSystemResourceAsStream(propertiesFile));
            } catch (IOException e1) {
                logger.debug("Cannot load property:" + propertiesFile, e1);
            }
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    logger.debug("Cannot load property:" + propertiesFile, e);
                }
            }
        }
        return properties;
    }

    /**
     * Current path finder.
     * 
     * @return path.
     */
    public static String currentPath() {
        File file = new File(".");
        if (file.getAbsolutePath().endsWith("bin" + File.separator + ".")) {
            return file.getAbsolutePath() + File.separator + ".." + File.separator;
        } else {
            return file.getAbsolutePath() + File.separator;
        }
    }

}
