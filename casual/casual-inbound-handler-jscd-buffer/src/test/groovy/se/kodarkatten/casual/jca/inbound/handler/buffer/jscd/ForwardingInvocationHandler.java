package se.kodarkatten.casual.jca.inbound.handler.buffer.jscd;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ForwardingInvocationHandler implements InvocationHandler
{
    private Object target;

    public ForwardingInvocationHandler(Object target )
    {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        Class<?>[] paramClass = null;
        if( args != null )
        {
            paramClass = new Class<?>[args.length];
            for( int i=0; i< args.length; i++ )
            {
                paramClass[i] = args[i].getClass();
            }
        }
        return target.getClass().getMethod( method.getName(), paramClass ).invoke( target, args );
    }
}
