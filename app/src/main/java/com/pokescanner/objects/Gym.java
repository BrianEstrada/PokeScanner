package com.pokescanner.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by Brian on 7/22/2016.
 */
@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Gym {
    double latitude,longitude;

}
