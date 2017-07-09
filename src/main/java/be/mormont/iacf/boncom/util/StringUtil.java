package be.mormont.iacf.boncom.util;

/**
 * Date: 09-07-17
 * By  : Mormont Romain
 */
public class StringUtil {
    public static String getNotEmptyOrNull(String s) {
        return s == null || s.trim().isEmpty() ? null : s.trim();
    }
}
