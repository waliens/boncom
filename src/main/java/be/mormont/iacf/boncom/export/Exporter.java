package be.mormont.iacf.boncom.export;

import java.io.IOException;

/**
 * Date: 11-07-17
 * By  : Mormont Romain
 */
public interface Exporter<T> {
    void export(String filepath, T object) throws IOException;
}
