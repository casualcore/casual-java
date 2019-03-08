/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.buffer.type.fielded.marshalling;

/**
 * {@code STRICT} mode:
 * marshalling - if a value is null, FieldedMarshallingException will be thrown
 * unmarshalling - if a value is missing in the buffer, FieldedUnmarshallingException will be thrown
 *
 * {@code RELAXED} mode:
 * marshalling - if a value is null nothing happens except there will be no marshalled value
 * unmarshalling - if a value is missing in the buffer - no initialization will take place for that value
 */
public enum FieldedTypeBufferProcessorMode
{
    STRICT,
    RELAXED
}
