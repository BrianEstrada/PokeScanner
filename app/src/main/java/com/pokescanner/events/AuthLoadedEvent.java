package com.pokescanner.events;

import com.pokescanner.objects.GoogleAuthToken;

/**
 * Created by Brian on 7/22/2016.
 */
public class AuthLoadedEvent {
    public static final int OK = 1;
    public static final int AUTH_FAILED = 2;
    public static final int SERVER_FAILED = 3;
    int status;
    GoogleAuthToken token;

    public AuthLoadedEvent(int status) {
        this.status = status;
    }

    public GoogleAuthToken getToken() {
        return token;
    }

    public void setToken(GoogleAuthToken token) {
        this.token = token;
    }

    public AuthLoadedEvent(int status, GoogleAuthToken token) {

        this.status = status;
        this.token = token;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
