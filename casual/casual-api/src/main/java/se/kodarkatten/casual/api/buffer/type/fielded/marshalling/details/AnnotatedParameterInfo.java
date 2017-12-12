package se.kodarkatten.casual.api.buffer.type.fielded.marshalling.details;

import se.kodarkatten.casual.api.buffer.type.fielded.annotation.CasualFieldElement;
import se.kodarkatten.casual.api.buffer.type.fielded.marshalling.FieldedUnmarshallingException;

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
public final class AnnotatedParameterInfo
{
    private final CasualFieldElement annotation;
    private final Class<?> type;
    private final Optional<ParameterizedType> parameterizedType;
    public AnnotatedParameterInfo(final CasualFieldElement annotation, final Class<?> type, final Optional<ParameterizedType> parameterizedType)
    {
        this.annotation = annotation;
        this.type = type;
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

    public CasualFieldElement getAnnotation()
    {
        return annotation;
    }

    public Class<?> getType()
    {
        return type;
    }

    public ParameterizedType getParameterizedType()
    {
        return parameterizedType.orElseThrow(() -> new FieldedUnmarshallingException("missing parameterized type!"));
    }
}
