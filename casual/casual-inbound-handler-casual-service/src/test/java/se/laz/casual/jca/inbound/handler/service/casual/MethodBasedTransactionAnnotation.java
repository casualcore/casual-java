/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inbound.handler.service.casual;

import se.laz.casual.api.service.CasualService;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

public class MethodBasedTransactionAnnotation implements SomeInterface
{
    @CasualService(name="someMethod" )
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    @Override
    public void someMethod()
    {

    }
}
