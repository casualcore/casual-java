/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.buffer.type.fielded.marshalling.details;

import se.laz.casual.api.buffer.type.fielded.annotation.CasualFieldElement;
import se.laz.casual.api.buffer.type.fielded.marshalling.FieldedUnmarshallingException;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.util.Objects;
import java.util.Optional;

/**
 * AnnotatedParameterInfo
 * Holds information regarding one @CasualFieldElement annotated parameter
 */
public final class AnnotatedParameterInfo extends ParameterInfo
{
    private final CasualFieldElement annotation;
    private final ParameterizedType parameterizedType;
    private AnnotatedParameterInfo(final CasualFieldElement annotation, final Class<?> type, final ParameterizedType parameterizedType)
    {
        super(type);
        this.annotation = annotation;
        this.parameterizedType = parameterizedType;
    }

    /**
     * Creates a new instance of ParameterInfo
     * @param annotation the annotation
     * @param type the type
     * @param parameterizedType the parameterized type - if any
     * @return a new instance
     */
    public static ParameterInfo of(final Annotation annotation, final Class<?> type, final ParameterizedType parameterizedType)
    {
        Objects.requireNonNull(annotation, "annotation can not be null");
        if(!(annotation instanceof CasualFieldElement))
        {
            throw new FieldedUnmarshallingException("expected @CasualFieldElement, not : " + annotation);
        }
        return new AnnotatedParameterInfo((CasualFieldElement) annotation, type, parameterizedType);
    }

    /**
     * Get the annotation
     * @return the annotation
     */
    public CasualFieldElement getAnnotation()
    {
        return annotation;
    }

    /**
     * Get the parameterized type
     * @throws FieldedUnmarshallingException if there is no parameterized type
     * @return the parameterized type
     */
    public ParameterizedType getParameterizedType()
    {
        return Optional.ofNullable(parameterizedType).orElseThrow(() -> new FieldedUnmarshallingException("missing parameterized type!"));
    }


}
