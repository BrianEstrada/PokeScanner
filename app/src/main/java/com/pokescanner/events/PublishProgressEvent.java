package com.pokescanner.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by Brian on 7/23/2016.
 */
@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)

public class PublishProgressEvent {
    int progress;
}
