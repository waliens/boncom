package be.mormont.iacf.boncom;


import java.util.logging.Logger;

/**
 * Created by Romain on 03-07-17.
 * This is a class for helper logger management
 */
public class Lg {
    public static Logger getLogger(String className) {
        return Logger.getLogger(className);
    }

    public static Logger getLogger(Class cls) {
        return Logger.getLogger(cls.getName());
    }

}
