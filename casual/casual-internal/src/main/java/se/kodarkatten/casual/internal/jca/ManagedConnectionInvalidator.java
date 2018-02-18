package se.kodarkatten.casual.internal.jca;

@FunctionalInterface
public interface ManagedConnectionInvalidator
{
    void invalidate(Exception e);
}
