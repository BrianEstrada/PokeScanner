package com.pokescanner.helper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Settings {
    boolean boundingBoxEnabled;
    int serverRefresh;
    int mapRefresh;
}
