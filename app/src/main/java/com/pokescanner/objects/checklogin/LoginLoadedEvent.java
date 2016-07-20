package com.pokescanner.objects.checklogin;

import POGOProtos.Networking.EnvelopesOuterClass;

/**
 * Created by Brian on 7/19/2016.
 */
public class LoginLoadedEvent {
    EnvelopesOuterClass.Envelopes.RequestEnvelope.AuthInfo authInfo;

    public LoginLoadedEvent(EnvelopesOuterClass.Envelopes.RequestEnvelope.AuthInfo authInfo) {
        this.authInfo = authInfo;
    }

    public EnvelopesOuterClass.Envelopes.RequestEnvelope.AuthInfo getAuthInfo() {
        return authInfo;
    }

    public void setAuthInfo(EnvelopesOuterClass.Envelopes.RequestEnvelope.AuthInfo authInfo) {
        this.authInfo = authInfo;
    }
}
