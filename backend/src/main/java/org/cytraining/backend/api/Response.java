package org.cytraining.backend.api;

/**
 * Wrapper for the api response strucure expected by the frontend.
 */
public class Response<T> {
    public boolean success;
    // null if success is true
    public String error;
    // null if success is false
    public T data;

    private Response(boolean success, String error, T data) {
        this.success = success;
        this.error = error;
        this.data = data;
    }

    public static <T> Response<T> ok(T data) {
        return new Response<>(true, null, data);
    }

    public static <T> Response<T> error(String msg) {
        return new Response<>(false, msg, null);
    }
}