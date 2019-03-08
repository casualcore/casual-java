/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.queue;

import java.util.Objects;
import java.util.UUID;

/**
 * Message selector for selecting message from a queue
 */
public final class MessageSelector
{
    private final String selector;
    private final UUID selectorId;
    private  MessageSelector(final String selector, final UUID selectorId)
    {
        this.selector = selector;
        this.selectorId = selectorId;
    }

    /**
     * Create an MessageSelector instance
     * @param selector the selector
     * @param selectorId the id
     * @return a MessageSelector
     */
    public static MessageSelector of(final String selector, final UUID selectorId)
    {
        Objects.requireNonNull(selector, "selector is not allowed to be null");
        Objects.requireNonNull(selectorId, "selectorId is not allowed to be null");
        return new MessageSelector(selector, selectorId);
    }

    /**
     * Creates a null selector
     * @return a MessageSelector
     */
    public static MessageSelector of()
    {
        return of("", new UUID(0,0));
    }

    /**
     * Creates a selector only using a {@link String} selector
     * @param selector the selector string
     * @return a MessageSelector
     */
    public static MessageSelector of(final String selector)
    {
        return of(selector, new UUID(0,0));
    }

    /**
     * Creates a selector only using a {@link UUID} selector
     * @param selectorId the selector id
     * @return a MessageSelector
     */
    public static MessageSelector of(final UUID selectorId)
    {
        return of("", selectorId);
    }

    /**
     * Get the selector
     * @return the selector
     */
    public String getSelector()
    {
        return selector;
    }

    /**
     * Get the selector id
     * @return the selector id
     */
    public UUID getSelectorId()
    {
        return selectorId;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        MessageSelector that = (MessageSelector) o;
        return Objects.equals(selector, that.selector) &&
            Objects.equals(selectorId, that.selectorId);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(selector, selectorId);
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("MessageSelector{");
        sb.append("selector='").append(selector).append('\'');
        sb.append(", selectorId=").append(selectorId);
        sb.append('}');
        return sb.toString();
    }
}
