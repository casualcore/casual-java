/*
 * Copyright (c) 2017 - 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inbound.handler.service.casual;

import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import se.laz.casual.api.service.CasualService;

// java:S1186 - test code
@SuppressWarnings("java:S1186")
public class MethodBasedTransactionAnnotation implements SomeInterface
{
    @CasualService(name="someMethod" )
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    @Override
    public void someMethod()
    {

    }
}
