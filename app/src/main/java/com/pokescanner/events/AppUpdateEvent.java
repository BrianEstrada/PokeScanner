package com.pokescanner.events;

import com.pokescanner.updater.AppUpdate;

import lombok.Getter;

@Getter
public class AppUpdateEvent {
    public static final int OK = 1;
    public static final int FAILED = 2;

    AppUpdate appUpdate;
    int status;

    public AppUpdateEvent(int status) { this.status = status; }
    public AppUpdateEvent(int status, AppUpdate update) {
        this.status = status;
        this.appUpdate = update;
    }
}
