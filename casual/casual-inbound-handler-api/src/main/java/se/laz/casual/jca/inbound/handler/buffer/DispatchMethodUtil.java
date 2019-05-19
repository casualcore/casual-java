package se.laz.casual.jca.inbound.handler.buffer;

import java.lang.reflect.Method;

/**
 * Helper class to facilitate checks and conversion for calling methods with correct parameters.
 */
public class DispatchMethodUtil
{
    private DispatchMethodUtil()
    {

    }

    /**
     * Check if the method has a single parameter that is of parameter type.
     *
     * @param method to check parameters.
     * @param param that will be sent.
     * @return if the method accepts the param or not.
     */
    public static boolean methodAccepts(Method method, Object param )
    {
        return method.getParameterCount() == 1 && method.getParameterTypes()[0].isAssignableFrom( param.getClass() );
    }

    /**
     * Convert the parameter to be sent to the method into the required Object[]
     * Will perform necessary casts as required.
     *
     * @param method that will recieve the parameters.
     * @param param that will be sent.
     * @return the appropriately cast parameters.
     * @throws ClassCastException if the parameter cannot be appropriately cast, calls to this method should therefore
     *               be performed after checking with {@link #methodAccepts(Method, Object)}.
     *
     */
    public static Object[] toMethodParams(Method method, Object param )
    {
        return new Object[]{method.getParameterTypes()[0].cast( param )};
    }
}
