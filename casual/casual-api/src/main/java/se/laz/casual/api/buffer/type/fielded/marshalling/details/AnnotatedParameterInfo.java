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
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * AnnotatedParameterInfo
 * Holds information regarding one @CasualFieldElement annotated parameter
 */
// sonar hates lambdas
@SuppressWarnings("squid:S1612")
public final class AnnotatedParameterInfo extends ParameterInfo
{
    private final CasualFieldElement annotation;
    private final Optional<ParameterizedType> parameterizedType;
    public AnnotatedParameterInfo(final CasualFieldElement annotation, final Class<?> type, final Optional<ParameterizedType> parameterizedType)
    {
        super(type);
        this.annotation = annotation;
        this.parameterizedType = parameterizedType;
    }
    public static AnnotatedParameterInfo of(final CasualFieldElement annotation, final Class<?> type, final Optional<ParameterizedType> parameterizedType)
    {
        Objects.requireNonNull(annotation, "annotation can not be null");
        Objects.requireNonNull(type, "type is not allowed to be null");
        return new AnnotatedParameterInfo(annotation, type, parameterizedType);
    }

    public static AnnotatedParameterInfo of(final List<Annotation> annotations, final Class<?> type, final Optional<ParameterizedType> parameterizedType)
    {
        CasualFieldElement cfe = annotations.stream()
                                            .filter(v -> v instanceof CasualFieldElement)
                                            .map(a -> CasualFieldElement.class.cast(a))
                                            .findFirst()
                                            .orElseThrow(() -> new FieldedUnmarshallingException("missing @CasualFieldElement"));
        return AnnotatedParameterInfo.of(cfe, type, parameterizedType);
    }

    public static ParameterInfo of(final Annotation annotation, final Class<?> type, final Optional<ParameterizedType> parameterizedType)
    {
        Objects.requireNonNull(annotation, "annotation can not be null");
        Objects.requireNonNull(type, "type can not be null");
        if(!(annotation instanceof CasualFieldElement))
        {
            throw new FieldedUnmarshallingException("expected @CasualFieldElement, not : " + annotation);
        }
        return new AnnotatedParameterInfo((CasualFieldElement) annotation, type, parameterizedType);
    }

    public CasualFieldElement getAnnotation()
    {
        return annotation;
    }

    public ParameterizedType getParameterizedType()
    {
        return parameterizedType.orElseThrow(() -> new FieldedUnmarshallingException("missing parameterized type!"));
    }


}
