package se.kodarkatten.casual.jca.inbound.handler.service.casual

import se.kodarkatten.casual.api.service.CasualService
import spock.lang.Shared
import spock.lang.Specification

import javax.ejb.TransactionAttributeType

class TransactionAttributeTypeFinderTest extends Specification
{
    @Shared CasualServiceMetaData methodBasedAnnotation = createCasualEntry( MethodBasedTransactionAnnotation.class )
    @Shared CasualServiceMetaData classBasedAnnotation = createCasualEntry( ClassBasedTransactionAnnotation.class )
    @Shared CasualServiceMetaData classAndMethodBasedAnnotation = createCasualEntry( ClassAndMethodBasedTransactionAnnotation.class )
    @Shared CasualServiceMetaData noAnnotation = createCasualEntry( NoAnnotation.class )

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

    CasualServiceMetaData createCasualEntry(Class<?> classInfo )
    {
        return CasualServiceMetaData.newBuilder()
                .service( classInfo.getMethod( "someMethod" ).getAnnotation(CasualService.class) )
                .serviceMethod( classInfo.getMethod( "someMethod" ) )
                .implementationClass( classInfo )
                .build()
    }
}
