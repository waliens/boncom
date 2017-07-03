package be.mormont.iacf.boncom.db;

/**
 * Created by Romain on 03-07-17.
 * This is a class.
 */
public interface Callback<T> {
    // Handling failure
    default void onFailure(Exception e) { failure(e); }
    default void failure(Exception e) { failure(); }
    default void failure() {} // TO IMPLEMENT
    // Handling success
    default void onSuccess(T object) { success(object); }
    void success(T object); // TO IMPLEMENT
}
