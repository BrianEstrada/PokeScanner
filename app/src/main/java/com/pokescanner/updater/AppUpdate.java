package com.pokescanner.updater;

import lombok.Getter;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Getter
public class AppUpdate {
    private String assetUrl;
    private String version;
    private String changelog;
}
