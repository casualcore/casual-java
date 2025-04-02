/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test.k8s.runtime;

/**
 * Determine if the currently executing process is running inside a container or not.
 * <br/>
 * This check is based on the expectation that container runtimes will inject an
 * environment variable of the name "container" into their runtime.
 * <br/>
 * It is also based on the expectation that the integration tests running will not
 * manually set the "container" environment variable themselves.
 * <br/>
 * I have tested this in k3s and openshift, though struggled to find any definitive
 * documentation which states this is how it should be.
 */
public class ContainerAwareness
{

    public static final String CONTAINER_ENV = "container";

    private static boolean container = false;

    static
    {
        init();
    }

    static void init()
    {
        String containerEnv = System.getenv( CONTAINER_ENV );
        container = containerEnv != null && !containerEnv.isEmpty();
    }

    public static boolean inContainer()
    {
        return container;
    }
}
