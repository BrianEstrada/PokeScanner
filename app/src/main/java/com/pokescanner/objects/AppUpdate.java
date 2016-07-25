package com.pokescanner.objects;

import lombok.Getter;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Getter
public class AppUpdate {
    private String assetUrl;
    private String version;
    private String changelog;
}
