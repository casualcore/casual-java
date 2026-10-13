/*
 * Copyright (c) 2023, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca;

public interface ConnectionObserver
{
    /**
     * Notifies you when a remote domain's topology changes.
     *
     * <p>The network event loop for the observed connection invokes this callback. Return
     * promptly and do not issue outbound calls from it. Each network connection has its own
     * observer.
     *
     * @param domainId the remote domain whose topology changed
     */
    void topologyChanged(DomainId domainId);

    /**
     * Notifies you when a remote domain starts disconnecting.
     *
     * <p>The network event loop for the observed connection invokes this callback after it
     * records the disconnect. Return promptly and do not issue outbound calls from it. Each
     * network connection has its own observer.
     *
     * @param domainId the remote domain that starts disconnecting
     */
    void domainIsDisconnecting(DomainId domainId);
}
