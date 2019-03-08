/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api;

/**
 * Not implemented yet
 * @author jone
 */
public interface CasualConversationAPI
{
    void tpconnect();
    void tpdiscon();
    void tprecv();

    void tpreturn();

    void tpsend();
}
