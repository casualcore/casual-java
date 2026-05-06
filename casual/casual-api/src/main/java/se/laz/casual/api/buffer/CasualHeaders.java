/*
 * Copyright (c) 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.buffer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Headers associated with casual buffers.
 * <br/>
 * Headers are defined as key:value strings.
 * <br/>
 * Header keys/names can occur multiple times.
 * <br/>
 * Therefore retrieval of a named header returns a {@link List} of values associated with the
 * named header.
 */
public class CasualHeaders
{
    private static final String COLON_SEP = ":";
    private static final CasualHeaders EMPTY = CasualHeaders.newBuilder().build();

    /**
     * Instance of CasualHeaders without any entries - empty.
     * @return empty instance of casual headers.
     */
    public static CasualHeaders empty()
    {
        return EMPTY;
    }

    private final List<String> order;
    private final Map<String,List<String>> entries;

    private CasualHeaders( Builder builder )
    {
        this.order = builder.order;
        this.entries = builder.entries;
    }

    /**
     * Retrieve header by key/name.
     *
     * @param name of the header to retrieve.
     * @return all values associated with the named header.
     */
    public List<String> get( String name )
    {
        Objects.requireNonNull( name, "Name is null." );
        if( !entries.containsKey( name ) )
        {
            return Collections.emptyList();
        }
        return new ArrayList<>( entries.get( name ) );
    }

    /**
     * Retrieve all header keys/names present.
     *
     * @return unique list of headers present.
     */
    public Set<String> getNames( )
    {
        return new HashSet<>( entries.keySet() );
    }

    /**
     * Retrieve all header "key:value" entries in insertion order.
     *
     * @return List of all "key:value" headers.
     */
    public List<String> getAll( )
    {
        List<String> all = new ArrayList<>(order.size());
        Map<String,Integer> indexes = new HashMap<>();
        for( String name: order )
        {
            int index = indexes.getOrDefault( name, 0 );
            String value = entries.get( name ).get( index );
            all.add( name + COLON_SEP + value );
            indexes.put( name, ++index );
        }
        return all;
    }

    /**
     * Number of headers stored.
     * <br/>
     * If a header with the same name is added multiple times,
     * this will count each instance.
     *
     * @return number of headers.
     */
    public int size()
    {
        return this.order.size();
    }

    /**
     * Check if a named header is present.
     *
     * @param name of the header to check.
     * @return if present.
     */
    public boolean containsName( String name )
    {
        Objects.requireNonNull( name, "Name is null." );
        return entries.containsKey( name );
    }

    @Override
    public boolean equals( Object o )
    {
        if( o == null || getClass() != o.getClass() )
        {
            return false;
        }
        CasualHeaders that = (CasualHeaders) o;
        return Objects.equals( order, that.order ) && Objects.equals( entries, that.entries );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( order, entries );
    }

    @Override
    public String toString()
    {
        return "CasualHeaders{" + "order=" + order +
                ", entries=" + entries +
                '}';
    }

    public static Builder newBuilder()
    {
        return new Builder();
    }

    public static Builder newBuilder( CasualHeaders src )
    {
        CasualHeaders.Builder b = CasualHeaders.newBuilder();

        src.entries.forEach( ( key, value ) -> b.entries.put( key, new ArrayList<>( value ) ) );
        b.order.addAll( src.order );

        return b;
    }

    public static final class Builder
    {
        private final Map<String, List<String>> entries = new HashMap<>();
        private final List<String> order = new ArrayList<>();

        /**
         * Add a "key:value" formatted string header entry.
         *
         * @param header the "key:value" header entry.
         * @return the builder.
         */
        public Builder add( String header )
        {
            Objects.requireNonNull( header, "header is null." );
            int index = header.indexOf( COLON_SEP );
            if( index == -1 )
            {
                throw new IllegalArgumentException( "Header entry must contain a : " + header );
            }
            return this.add( header.substring( 0, index ), header.substring( index +1 ) );
        }

        /**
         * Add a header entry.
         *
         * @param name of the header.
         * @param value of the header.
         * @return the builder.
         */
        public Builder add( String name, String value )
        {
            Objects.requireNonNull( name, "Name is null." );
            Objects.requireNonNull( value, "Value is null." );
            order.add( name );
            entries.computeIfAbsent( name, e -> new ArrayList<>() ).add( value );
            return this;
        }

        /**
         * Add a list of "key:value" formatted string header entries.
         *
         * @param headers to add.
         * @return the builder.
         */
        public Builder addAll( List<String> headers )
        {
            Objects.requireNonNull( headers, "Headers is null." );
            headers.forEach( this::add );
            return this;
        }

        public CasualHeaders build()
        {
            return new CasualHeaders( this );
        }
    }
}
