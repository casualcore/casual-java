/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.internal;

/**
 * These are APIs that will be used internally to advertise to casual which services are available
 *
 * These should not be used by application developers directly. Instead they should u
 *
 * @author jone
 */
public interface CasualInternalAPI
{

    void tpadvertise();
    void tpunadvertise();

    void tpcancel();


    void tpalloc();

    void tpfree();
    void tprealloc();

    void tpgetrply();

    void tpservice();

    void tptypes();


}
