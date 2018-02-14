package se.kodarkatten.casual.jca.inbound.handler.service.casual

import se.kodarkatten.casual.api.services.CasualService
import spock.lang.Shared
import spock.lang.Specification

import javax.ejb.TransactionAttributeType

class TransactionAttributeTypeFinderTest extends Specification
{
    @Shared CasualServiceEntry methodBasedAnnotation = createCasualEntry( MethodBasedTransactionAnnotation.class )
    @Shared CasualServiceEntry classBasedAnnotation = createCasualEntry( ClassBasedTransactionAnnotation.class )
    @Shared CasualServiceEntry classAndMethodBasedAnnotation = createCasualEntry( ClassAndMethodBasedTransactionAnnotation.class )
    @Shared CasualServiceEntry noAnnotation = createCasualEntry( NoAnnotation.class )

    def "Casual Entry with a method based annotation returns method based value."()
    {
        expect:
        TransactionAttributeTypeFinder.find( methodBasedAnnotation ) == TransactionAttributeType.REQUIRES_NEW
    }

    def "Casual Entry with a class and method based annotation returns method based value."()
    {
        expect:
        TransactionAttributeTypeFinder.find( classAndMethodBasedAnnotation ) == TransactionAttributeType.NOT_SUPPORTED
    }

    def "CasualEntry with a class annotation but no method, returns class based value."()
    {
        expect:
        TransactionAttributeTypeFinder.find( classBasedAnnotation ) == TransactionAttributeType.NEVER
    }

    def "CasualEntry with no annotations on class or method, returns required."()
    {
        expect:
        TransactionAttributeTypeFinder.find( noAnnotation ) == TransactionAttributeType.REQUIRED
    }

    def "find null CasualEntry throws NullPointerException"()
    {
        when:
        TransactionAttributeTypeFinder.find( null )

        then:
        thrown NullPointerException.class
    }

    CasualServiceEntry createCasualEntry( Class<?> classInfo )
    {
        return new CasualServiceEntry( classInfo.getAnnotation(CasualService.class), classInfo.getMethod( "someMethod" ), classInfo )
    }
}
