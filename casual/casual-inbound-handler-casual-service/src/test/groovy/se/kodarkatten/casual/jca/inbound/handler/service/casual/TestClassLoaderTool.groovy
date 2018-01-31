package se.kodarkatten.casual.jca.inbound.handler.service.casual

class TestClassLoaderTool
{
    /**
     * Create a Classloader that has only the following jar files.
     *
     * @param jars
     * @return classloader
     */
    static ClassLoader createClassLoader( File...jars )
    {
        URL[] resources = new URL[jars.length]
        for (int i; i < jars.length; i++)
        {
            File f = jars[i]
            resources[i] = f.toURI().toURL()
        }
        return new URLClassLoader(resources, (ClassLoader) null)
    }
}
