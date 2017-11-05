package se.kodarkatten.casual.internal.thread;

public class ThreadClassLoaderTool
{
    private final ClassLoader initial;

    public ThreadClassLoaderTool()
    {
        initial = Thread.currentThread().getContextClassLoader();
    }

    public void loadClassLoader(Object object )
    {
        ClassLoader c = object.getClass().getClassLoader();
        Thread.currentThread().setContextClassLoader( c );
    }

    public void revertClassLoader()
    {
        Thread.currentThread().setContextClassLoader( initial );
    }

}
