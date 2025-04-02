/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test.k8s.store;

import java.util.Map;

/**
 * Generic store for resources providing standard CRUD functionality.
 *
 * @param <T> the resource type the store handles.
 */
public interface ResourceStore<T>
{
    /**
     * Retrieve the resource stored by name.
     *
     * @param name of the resource
     * @return the resource matching the name.
     * @throws ResourceNotFoundException if not stored.
     */
    T get( String name );

    /**
     * Retrieve all the stored resources.
     *
     * @return map of resources.
     */
    Map<String,T> getAll();

    /**
     * Store the resource with the provided name.
     *
     * @param name of the resource, used for retrieval later.
     * @param value the resource to store.
     */
    void put( String name, T value );

    /**
     *
     * @param all
     */
    void putAll( Map<String,T> all );

    /**
     * Remove the resource stored by name.
     *
     * @param name of the resource to remove.
     * @return the resource matching the name.
     * @throws ResourceNotFoundException if not stored.
     */
    T remove( String name );
}
