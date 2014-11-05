package com.covidien.util;

import java.io.File;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * Class to have utility functions.
 * 
 * @author philip.ye
 */
public final class Utility {

    /**
     * Private constructor.
     */
    private Utility() {

    }

    /**
     * Log4j Properties to load.
     */
    private static String log4jPropertyPath = "src" + File.separator + "main" + File.separator + "resources"
            + File.separator + "log4j.properties";

    /**
     * Logger object.
     */
    private static Logger logger = Utility.getLogger(Utility.class);

    /**
     * Create a singleton Logger object and return.
     * 
     * @param clazz
     *        name of the class.
     * @return Logger object.
     */
    public static synchronized Logger getLogger(@SuppressWarnings("rawtypes") final Class clazz) {
        if (logger == null) {
            PropertyConfigurator.configure(currentPath() + File.separator + log4jPropertyPath);
            logger = Logger.getLogger(clazz);
            return logger;
        }
        if (logger != null && !logger.getName().equals(clazz.getName())) {
            logger = Logger.getLogger(clazz);
        }
        return logger;
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
