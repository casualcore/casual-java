package se.kodarkatten.casual.api.service

import se.kodarkatten.casual.network.messages.domain.TransactionType
import spock.lang.Shared
import spock.lang.Specification

class ServiceInfoTest extends Specification
{

    @Shared ServiceInfo instance
    @Shared String serviceName = "servicename"
    @Shared String category = "categoryname"
    @Shared TransactionType transactionType = TransactionType.ATOMIC

    def setup()
    {
        instance = ServiceInfo.of( serviceName, category, transactionType )
    }

    def "Get service name."()
    {
        expect:
        instance.getServiceName() == serviceName
    }

    def "Get category"()
    {
        expect:
        instance.getCategory() == category
    }

    def "Get transaction type"()
    {
        expect:
        instance.getTransactionType() == transactionType
    }
}
