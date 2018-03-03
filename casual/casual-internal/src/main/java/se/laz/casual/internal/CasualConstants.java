/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.internal;

/**
 * Created by jone on 2017-03-15.
 */
public final class CasualConstants
{
    //Note this is done this way because we do not want compiler inlining
    public static final String CASUAL_API_VERSION = str("1.0");
    public static final String CASUAL_NAME = str("casual-middleware");
    public static final String CASUAL_ADAPTER_NAME = str("casual-jca-connector");
    public static final String CASUAL_ADAPTER_DESCRIPTION = str("A XA Connector to Casual EIS");
    public static final String CASUAL_ADAPTER_JCA_SPEC_VERSION = str("1.7");
    private CasualConstants()
    {}
    private static String str(String s)
    {
        return s;
    }
}
