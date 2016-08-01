package com.pokescanner.updater;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class AppUpdate {
    private String assetUrl;
    private String version;
    private String changelog;
}
