/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.event;

import java.util.Arrays;

public enum Order
{
    // note: really means inbound
    // It is the same way as it is in casual
    SEQUENTIAL('S'),
    // note: really means outbound
    // It is the same way as it is in casual
    CONCURRENT('C');
    private char value;
    Order(char value)
    {
        this.value = value;
    }

    public char getValue()
    {
        return value;
    }

    public static Order unmarshall(char value)
    {
        return Arrays.stream(Order.values())
                     .filter(item -> item.getValue() == value)
                     .findFirst()
                     .orElseThrow(() -> new IllegalArgumentException("value: " + value));
    }

    public static char marshall(Order order)
    {
        return order.getValue();
    }

}