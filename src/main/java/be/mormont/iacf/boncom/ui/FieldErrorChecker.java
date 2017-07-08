package be.mormont.iacf.boncom.ui;

import java.util.HashMap;

/**
 * Date: 05-07-17
 * By  : Mormont Romain
 */
class FieldErrorChecker extends HashMap<String, String> {
    String whichEmpty() {
        for (Entry<String, String> keyValue : this.entrySet()) {
            if (keyValue.getValue().isEmpty()) {
                return keyValue.getKey();
            }
        }
        return null;
    }
}
